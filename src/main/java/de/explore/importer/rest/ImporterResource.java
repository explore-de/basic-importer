package de.explore.importer.rest;

import de.exentra.ads.avro.generated.AvroTreeWrapper;
import de.exentra.explore.plm.avro.ProjectSyncObjectBuilder;
import de.explore.importer.dto.StartImportMultipartDTO;
import de.explore.importer.model.BomNode;
import de.explore.importer.service.CsvService;
import de.explore.importer.service.ImportService;
import de.explore.importer.service.ZipService;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Path("/import")
public class ImporterResource
{
	private static final Logger LOG = LoggerFactory.getLogger(ImporterResource.class);

	@Inject
	ImportService importService;

	@Inject
	ZipService zipService;

	@Inject
	CsvService csvService;

	@POST
	@Path("/start")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response startImport(StartImportMultipartDTO startImportMultipartDTO)
	{
		LOG.info("Start importing for {}", startImportMultipartDTO.getProjectName());

		// Check if project exists. If it exists id is returned, if not,
		// projected is created and id returned
		ProjectSyncObjectBuilder projectSyncObjectBuilder = importService.getOrCreateProject(startImportMultipartDTO.getProjectName());
		LOG.info("Project {} with id {}", projectSyncObjectBuilder.getName(), projectSyncObjectBuilder.getProjectUniqueId());

		// Extract files from zip file from request, contains csv file and 3d
		// files like cad
		Map<String, byte[]> extractedFiles = zipService.extractZipFileFromInputStream(startImportMultipartDTO.getZipFile());

		// check for csv file in zip file, make sure only 1 zip file is in zip
		// request file
		Optional<String> csvKey = extractedFiles.keySet().stream().filter(s -> s.endsWith(".csv")).findFirst();

		List<BomNode> bomNodes;
		if (csvKey.isPresent())
		{
			LOG.info("Found csv file {}", csvKey.get());
			// parse csv to pojos
			bomNodes = csvService.parseCSV(extractedFiles.get(csvKey.get()));
		}
		else
		{
			// throw exception when no csv file is found in request
			LOG.error("No csv file found in the request zip file");
			throw new BadRequestException("CSV file not found");
		}

		// create Avro tree from bom list
		AvroTreeWrapper avroTreeWrapper = importService.createAvroTreeWrapper(bomNodes, projectSyncObjectBuilder);
		// send tree to plm
		importService.sendAvroTreeToPlm(avroTreeWrapper, projectSyncObjectBuilder);

		// iterate over map and upload cad files to fileservice
		extractedFiles.forEach((k, v) -> {
			if (k.trim().toLowerCase().endsWith(".cad"))
			{
				importService.uploadToFileService(v, k, projectSyncObjectBuilder.getName());
			}
		});

		return Response.ok().build();
	}
}
