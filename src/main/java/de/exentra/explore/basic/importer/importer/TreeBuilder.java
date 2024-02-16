package de.exentra.explore.basic.importer.importer;

import static de.exentra.explore.plm.avro.NodeSyncObjectBuilder.TRAFO_IDENTITY_12V;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.exentra.ads.avro.generated.I18nObject;
import de.exentra.explore.basic.importer.pojo.Knot;
import de.exentra.explore.basic.importer.pojo.Node;
import de.exentra.explore.basic.importer.pojo.NodeAdapter;
import de.exentra.explore.plm.avro.DocumentSyncObjectBuilder;
import de.exentra.explore.plm.avro.DocumentVersionSyncObjectBuilder;
import de.exentra.explore.plm.avro.HasChildNodesBuilder;
import de.exentra.explore.plm.avro.MetaAttributesWriter;
import de.exentra.explore.plm.avro.NodeSyncObjectBuilder;
import de.exentra.explore.plm.avro.PartSyncObjectBuilder;
import de.exentra.explore.plm.avro.PartVersionSyncObjectBuilder;
import de.exentra.explore.plm.avro.PartViewSyncObjectBuilder;
import de.exentra.explore.plm.avro.ProjectSyncObjectBuilder;
import de.exentra.explore.plm.avro.TreeWrapperBuilder;
import de.exentra.explore.plm.avro.VariantRuleSyncObjectBuilder;
import io.micrometer.core.annotation.Timed;

public class TreeBuilder
{
	public static final String ADSMETA_VARIANT_CONDITION = "ADSMETA_variantCondition";
	public static final String ADSMETA_PNGUID = "ADSMETA_pnguid";
	public static final String ADSMETA_PNGUID_PARENT = "ADSMETA_pnguidParent";

	private final TreeWrapperBuilder treeWrapperBuilder;
	private final ProjectSyncObjectBuilder projectBuilder;

	private static final List<String> LIST = List.of("Z_ST", "Z_PBE", "Z_MODEL");

	public TreeBuilder(TreeWrapperBuilder treeWrapperBuilder, ProjectSyncObjectBuilder projectBuilder)
	{
		this.treeWrapperBuilder = treeWrapperBuilder;
		this.projectBuilder = projectBuilder;
	}

	public void buildTree(Knot root)
	{
		buildVariantRule(projectBuilder);
		buildTree(root, treeWrapperBuilder);
	}

	@Timed
	private void buildTree(Knot knot, HasChildNodesBuilder builder)
	{
		for (Knot child : knot.getChildren())
		{
			HasChildNodesBuilder builderChild = buildProjectTreeNode(child.getNode(), builder, child.getChildren().isEmpty());
			if (!child.getChildren().isEmpty())
			{
				buildTree(child, builderChild);
			}
		}
	}

	private void buildVariantRule(ProjectSyncObjectBuilder projectBuilder)
	{
		VariantRuleSyncObjectBuilder unconfigured = treeWrapperBuilder.findOrCreateVariantRule("Unconfigured");
		MetaAttributesWriter.getOrCreateCustomAttributesSyncObjectBuilder(unconfigured);
		treeWrapperBuilder.addReference(unconfigured.buildProjectReference(projectBuilder.getProjectUniqueId()));
		treeWrapperBuilder.addReference(projectBuilder.buildVariantRuleReferenceWithUniqueId(unconfigured));
	}

	@Timed
	private HasChildNodesBuilder buildProjectTreeNode(Node row, HasChildNodesBuilder builder, boolean isLeaf)
	{
		return buildVariantNode(builder, row, isLeaf);
	}

	private HasChildNodesBuilder buildVariantNode(HasChildNodesBuilder builder, Node row, boolean isLeaf)
	{
		String partNumber = row.getPname();
		String adsName = partNumber + "|" + row.getPnguid() + "|";

		NodeSyncObjectBuilder nodeBuilder = builder.findOrCreateChildNode(adsName, partNumber, row.getPntype());
		MetaAttributesWriter.writeAttributeValue(nodeBuilder, ADSMETA_VARIANT_CONDITION, row.getBzw());
		MetaAttributesWriter.writeAttributeValue(nodeBuilder, ADSMETA_PNGUID, row.getPnguid());
		MetaAttributesWriter.writeAttributeValue(nodeBuilder, ADSMETA_PNGUID_PARENT, row.getPnguidParent());

		buildPdmPartTreeAndReferenceToNodeAndDocument(row, nodeBuilder, adsName, isLeaf);

		return nodeBuilder;
	}

	@Timed
	private void buildPdmPartTreeAndReferenceToNodeAndDocument(Node row, NodeSyncObjectBuilder nodeBuilder, String adsName, boolean isLeaf)
	{
		boolean hasNodeAdapters = row.getNodeAdapters() != null && !row.getNodeAdapters().isEmpty();

		PartSyncObjectBuilder partBuilder = treeWrapperBuilder.findOrCreatePart(row.getPname())
			.description(row.getPnguidParent())
			.name(row.getPname());
		MetaAttributesWriter.writeAttributeValue(partBuilder, "ADSMETA_name", row.getName());

		PartVersionSyncObjectBuilder partVersionBuilder = partBuilder.findOrCreatePartVersion("A").description(buildPartVersionDescription(row.getName()));

		buildSimplePartView(row, nodeBuilder, partVersionBuilder, hasNodeAdapters, isLeaf);

		if (hasNodeAdapters)
		{
			for (int i = 1; i <= row.getNodeAdapters().size(); i++)
			{
				NodeAdapter nodeAdapter = row.getNodeAdapters().get(i - 1);

				NodeSyncObjectBuilder nodeNodeBuilder = nodeBuilder.findOrCreateChildNode((i + 1) + "|" + adsName, row.getPname(), nodeAdapter.getPntype());

				MetaAttributesWriter.writeAttributeValue(nodeNodeBuilder, ADSMETA_PNGUID, row.getPnguid());
				MetaAttributesWriter.writeAttributeValue(nodeNodeBuilder, ADSMETA_PNGUID_PARENT, row.getPnguidParent());
				MetaAttributesWriter.writeAttributeValue(nodeNodeBuilder, "ADSMETA_has_dmu_adapters", Boolean.TRUE);

				PartViewSyncObjectBuilder partViewBuilder = partVersionBuilder.findOrCreatePartView(formatTrafo(nodeAdapter)).isModel(LIST.contains(nodeAdapter.getPntype()));
				MetaAttributesWriter.writeAttributeValue(partViewBuilder, "ADSMETA_bzw", nodeAdapter.getBzw());
				MetaAttributesWriter.writeAttributeValue(partViewBuilder, "ADSMETA_matrix", nodeAdapter.getMatrix());
				MetaAttributesWriter.writeAttributeValue(partViewBuilder, "ADSMETA_cat_part", nodeAdapter.getCatPart());

				treeWrapperBuilder.addReference(nodeNodeBuilder.buildPartViewReference(partViewBuilder));
				treeWrapperBuilder.addReference(partViewBuilder.buildNodeReference(nodeNodeBuilder));

				buildDocumentsTreeAndReferenceToPart(nodeAdapter, partViewBuilder);
			}
		}
	}

	private void buildSimplePartView(Node row, NodeSyncObjectBuilder nodeBuilder, PartVersionSyncObjectBuilder partVersionBuilder, boolean hasDmuAdapters, boolean isLeaf)
	{
		PartViewSyncObjectBuilder partViewBuilder = partVersionBuilder.findOrCreatePartView(TRAFO_IDENTITY_12V).isModel(!hasDmuAdapters && isLeaf && LIST.contains(row.getPntype()));
		treeWrapperBuilder.addReference(nodeBuilder.buildPartViewReference(partViewBuilder));
		treeWrapperBuilder.addReference(partViewBuilder.buildNodeReference(nodeBuilder));
	}

	@Timed
	private void buildDocumentsTreeAndReferenceToPart(NodeAdapter row, PartViewSyncObjectBuilder partViewBuilder)
	{
		String filename = row.getCatPart();
		DocumentSyncObjectBuilder documentBuilder = treeWrapperBuilder.findOrCreateDocument(filename);
		documentBuilder.setDescription(row.getAdapterName());

		DocumentVersionSyncObjectBuilder documentVersionBuilder = documentBuilder.findOrCreateDocumentVersion("A").name(filename);
		documentVersionBuilder.findOrCreateDocumentDefinition(filename);

		treeWrapperBuilder.addReference(partViewBuilder.buildDocumentVersionReference(documentVersionBuilder));
		treeWrapperBuilder.addReference(documentVersionBuilder.buildPartViewReference(partViewBuilder));
	}

	private List<I18nObject> buildPartVersionDescription(String ob)
	{
		return List.of(new I18nObject(ob, Locale.ENGLISH));
	}

	public static String formatTrafo(NodeAdapter trafo)
	{
		try
		{
			List<String> trafoList = new ArrayList<>();
			trafoList.add(String.valueOf(trafo.getMatrixXx()));
			trafoList.add(String.valueOf(trafo.getMatrixYx()));
			trafoList.add(String.valueOf(trafo.getMatrixZx()));

			trafoList.add(String.valueOf(trafo.getMatrixXy()));
			trafoList.add(String.valueOf(trafo.getMatrixYy()));
			trafoList.add(String.valueOf(trafo.getMatrixZy()));

			trafoList.add(String.valueOf(trafo.getMatrixXz()));
			trafoList.add(String.valueOf(trafo.getMatrixYz()));
			trafoList.add(String.valueOf(trafo.getMatrixZz()));

			trafoList.add(String.valueOf(trafo.getMatrixX()));
			trafoList.add(String.valueOf(trafo.getMatrixY()));
			trafoList.add(String.valueOf(trafo.getMatrixZ()));
			return String.join("!", trafoList);
		}
		catch (Exception ex)
		{
			return TRAFO_IDENTITY_12V;
		}
	}
}
