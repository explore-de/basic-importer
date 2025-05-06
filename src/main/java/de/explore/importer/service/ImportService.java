package de.explore.importer.service;

import de.exentra.ads.avro.generated.AvroTreeWrapper;
import de.exentra.explore.plm.avro.MetaAttributesWriter;
import de.exentra.explore.plm.avro.NodeSyncObjectBuilder;
import de.exentra.explore.plm.avro.OemType;
import de.exentra.explore.plm.avro.PartSyncObjectBuilder;
import de.exentra.explore.plm.avro.PartVersionSyncObjectBuilder;
import de.exentra.explore.plm.avro.PartViewSyncObjectBuilder;
import de.exentra.explore.plm.avro.ProjectSyncObjectBuilder;
import de.exentra.explore.plm.avro.TreeWrapperBuilder;
import de.exentra.explore.plm.avro.VariantRuleSyncObjectBuilder;
import de.explore.importer.client.ImporterClient;
import de.explore.importer.client.ImporterClientBuilder;
import de.explore.importer.client.dto.Project;
import de.explore.importer.client.exception.ImporterClientException;
import de.explore.importer.client.kafka.AvroProducerConfig;
import de.explore.importer.model.BomItem;
import de.explore.importer.model.BomNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import okhttp3.ResponseBody;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

@ApplicationScoped
public class ImportService
{
	private static final Logger LOG = LoggerFactory.getLogger(ImportService.class);
	private final ImporterClient importerClient;
	private final String avroBucket;
	private final AvroProducerConfig avroProducerConfig;

	public ImportService()
	{
		Config config = ConfigProvider.getConfig();
		String fileServiceUrl = config.getConfigValue("de.explore.fileservice.url").getValue();
		String fileServiceToken = config.getConfigValue("de.explore.fileservice.token").getValue();
		String plmUrl = config.getConfigValue("de.explore.plm.url").getValue();
		String plmToken = config.getConfigValue("de.explore.plm.token").getValue();
		String kafkaBroker = config.getConfigValue("de.explore.kafka.broker").getValue();
		String kafkaTopic = config.getConfigValue("de.explore.kafka.topic").getValue();
		String avroUrl = config.getConfigValue("de.explore.avro.url").getValue();
		avroBucket = config.getConfigValue("s3.avro.bucket").getValue();

		ImporterClientBuilder builder = new ImporterClientBuilder();
		builder
			.getFileSrvConfig()
			.host(fileServiceUrl)
			.token(fileServiceToken);
		builder
			.getPlmSrvConfig()
			.host(plmUrl)
			.token(plmToken);
		importerClient = builder.build();
		avroProducerConfig = new AvroProducerConfig(kafkaBroker, kafkaTopic, avroUrl);
	}

	public Response<ResponseBody> uploadToFileService(byte[] fileAsByteArray, String fileName, String projectName)
	{
		try
		{
			String[] structure = { projectName, fileName };
			return importerClient.uploadFileToFileService(fileAsByteArray, fileName, structure, avroBucket);
		}
		catch (ImporterClientException e)
		{
			LOG.error(e.getMessage());
			throw new BadRequestException(e.getMessage());
		}
	}

	public ProjectSyncObjectBuilder getOrCreateProject(String projectName)
	{
		Project project = new Project(projectName, "testDescription", "testCategory", OemType.Alphafrog);
		LOG.info("Creating project {}", project);
		try
		{
			return importerClient.findOrCreateProject(project);
		}
		catch (IOException e)
		{
			LOG.error(e.getMessage());
			throw new BadRequestException(e.getMessage());
		}
	}

	public AvroTreeWrapper createAvroTreeWrapper(List<BomNode> nodes, ProjectSyncObjectBuilder projectSyncObjectBuilder)
	{
		LOG.info("Creating AvroTreeWrapper for list of nodes with size {}", nodes.size());
		TreeWrapperBuilder treeWrapperBuilder = importerClient.createTreeWrapperBuilder(projectSyncObjectBuilder.getProjectUniqueId(), OemType.Alphafrog);

		// create unconfigured variant rule
		VariantRuleSyncObjectBuilder unconfigured = treeWrapperBuilder.findOrCreateVariantRule("Unconfigured");

		// add variant rue reference to treebuilder, add references in both
		// directions
		treeWrapperBuilder.addReference(unconfigured.buildProjectReference(projectSyncObjectBuilder.getProjectUniqueId()));
		treeWrapperBuilder.addReference(projectSyncObjectBuilder.buildVariantRuleReferenceWithUniqueId(unconfigured));

		// Process all BOM root nodes
		for (BomNode bomRoot : nodes)
		{
			processBomNode(bomRoot, null, treeWrapperBuilder, projectSyncObjectBuilder);
		}

		return treeWrapperBuilder.build();
	}

	public void sendAvroTreeToPlm(AvroTreeWrapper avroTreeWrapper, ProjectSyncObjectBuilder projectSyncObjectBuilder)
	{
		try
		{
			importerClient.uploadAvroToPLM(avroTreeWrapper, avroProducerConfig, projectSyncObjectBuilder.getProjectUniqueId(), projectSyncObjectBuilder.getName(), "avro-bucket");

		}
		catch (ImporterClientException e)
		{
			LOG.error(e.getMessage());
			throw new InternalServerErrorException(e.getMessage());
		}
	}

	private void processBomNode(BomNode bomNode,
		NodeSyncObjectBuilder parentNode,
		TreeWrapperBuilder treeWrapperBuilder,
		ProjectSyncObjectBuilder projectSyncObjectBuilder)
	{
		BomItem item = bomNode.getItem();
		NodeSyncObjectBuilder currentNode;

		// Create the node – if there is no parent node, use the
		// treeWrapperBuilder
		if (parentNode == null)
		{
			currentNode = treeWrapperBuilder.findOrCreateChildNode(item.getPartId(), item.getPartId(), item.getComponent());
		}
		else
		{
			currentNode = parentNode.findOrCreateChildNode(item.getPartId(), item.getPartId(), item.getComponent());
		}

		// Create the part and assign the description
		PartSyncObjectBuilder partBuilder = treeWrapperBuilder.findOrCreatePart(item.getPartId())
			.description(item.getDescription());

		// Write custom attributes
		MetaAttributesWriter.writeAttributeValue(partBuilder, "ADSMETA_Supplier", item.getSupplier());
		MetaAttributesWriter.writeAttributeValue(partBuilder, "ADSMETA_Material", item.getMaterial());

		// Create part version and part view
		PartVersionSyncObjectBuilder partVersion = partBuilder.findOrCreatePartVersion("v1");
		PartViewSyncObjectBuilder partView = partVersion.findOrCreatePartView();

		// Add references between the node and the part view
		treeWrapperBuilder.addReference(partView.buildNodeReference(currentNode));
		treeWrapperBuilder.addReference(currentNode.buildPartViewReference(partView));

		// Recursively process all children of this node
		for (BomNode child : bomNode.getChildren())
		{
			processBomNode(child, currentNode, treeWrapperBuilder, projectSyncObjectBuilder);
		}
	}
}
