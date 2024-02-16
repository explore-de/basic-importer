package de.exentra.explore.basic.importer.importer;

import static java.security.MessageDigest.getInstance;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.xml.bind.annotation.adapters.HexBinaryAdapter;

import de.exentra.ads.avro.generated.AvroTreeWrapper;
import de.exentra.explore.basic.importer.pojo.Knot;
import de.exentra.explore.basic.importer.pojo.Node;
import de.exentra.explore.plm.avro.OemConfiguration;
import de.exentra.explore.plm.avro.OemType;
import de.exentra.explore.plm.avro.ProjectSyncObjectBuilder;
import de.exentra.explore.plm.avro.TreeWrapperBuilder;
import de.explore.api.importutils.utils.UploadUtils;

@ApplicationScoped
public class JsonTreeBuilder
{
	private static final Logger LOG = LoggerFactory.getLogger(JsonTreeBuilder.class);

	private final UploadUtils uploadUtils;

	private TreeWrapperBuilder treeWrapperBuilder;
	private Map<String, Knot> map;

	@Inject
	public JsonTreeBuilder(UploadUtils uploadUtils)
	{
		this.uploadUtils = uploadUtils;
	}

	public void buildTree(List<Node> nodeList) throws NoSuchAlgorithmException
	{
		map = new HashMap<>();
		Knot root = new Knot(null);
		map.put("", root);

		buildMap(nodeList);
		buildRootTree();

		String name = root.getChildren().get(0).getNode().getName();
		String classf = root.getChildren().get(0).getNode().getPvname();

		OemConfiguration.getInstance().setOemType(OemType.Porsche);
		ProjectSyncObjectBuilder projectBuilder = new ProjectSyncObjectBuilder(classf)
			.projectName(name)
			.category(classf)
			.description(name);

		long projectId = uploadUtils.checkProject(projectBuilder);
		treeWrapperBuilder = new TreeWrapperBuilder(104L, 105L, projectId, 11L, OemType.Porsche);

		buildTree(root, projectBuilder);

		String zipFileName = new HexBinaryAdapter().marshal(getInstance("MD5").digest((name).getBytes()));
		publishAvroTree(projectBuilder, zipFileName);
	}

	private void buildMap(List<Node> nodeList)
	{
		LOG.info("Building map...");
		long start = System.currentTimeMillis();

		for (Node node : nodeList)
		{
			String pnguid = node.getPnguid();

			if (map.containsKey(pnguid))
			{
				LOG.warn("Found already a valid knot");
				continue;
			}
			else
			{
				Knot knot = new Knot(node);
				map.put(pnguid, knot);
			}
		}

		long end = System.currentTimeMillis();
		LOG.info("Build map: {}ms", (end - start));
	}

	private void buildRootTree()
	{
		LOG.info("Building root tree...");
		long start = System.currentTimeMillis();

		for (Knot knot : map.values())
		{
			if (knot.getNode() == null)
			{
				continue;
			}

			Knot parent = map.get(knot.getNode().getPnguidParent());
			parent.getChildren().add(knot);
		}

		long end = System.currentTimeMillis();
		LOG.info("Build root tree: {}ms", (end - start));
	}

	private void buildTree(Knot root, ProjectSyncObjectBuilder projectBuilder)
	{
		LOG.info("Building tree for avro wrapper...");
		long start = System.currentTimeMillis();

		TreeBuilder treeBuilder = new TreeBuilder(treeWrapperBuilder, projectBuilder);
		treeBuilder.buildTree(root);

		long end = System.currentTimeMillis();
		LOG.info("Build tree for avro wrapper: {}ms", (end - start));
	}

	private void publishAvroTree(ProjectSyncObjectBuilder projectBuilder, String name)
	{
		AvroTreeWrapper tree = treeWrapperBuilder.build();
		uploadUtils.sendToPlm(tree, projectBuilder, name);
	}
}
