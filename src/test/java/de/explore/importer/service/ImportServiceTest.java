package de.explore.importer.service;

import de.exentra.ads.avro.generated.AvroTreeWrapper;
import de.exentra.explore.plm.avro.OemConfiguration;
import de.exentra.explore.plm.avro.OemType;
import de.exentra.explore.plm.avro.ProjectSyncObjectBuilder;
import de.explore.importer.model.BomNode;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.fail;

@QuarkusTest
class ImportServiceTest
{
	@Inject
	ImportService importService;

	@Inject
	CsvService csvService;

	@BeforeEach
	void setUp() throws IOException
	{
	}

	@Test
	void shouldTraverseAllBomNodes()
	{
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("train-bom.csv"))
		{
			byte[] csvBytes = inputStream.readAllBytes();
			List<BomNode> bomNodes = csvService.parseCSV(csvBytes);

			assertThat(bomNodes, is(not(emptyIterable())));
			assertThat(bomNodes.size(), is(1));

			OemConfiguration.getInstance().setOemType(OemType.Alphafrog);
			ProjectSyncObjectBuilder projectSyncObjectBuilder = new ProjectSyncObjectBuilder("testProject");
			projectSyncObjectBuilder.setProjectUniqueId(666L);

			AvroTreeWrapper testTreeWrapper = importService.createAvroTreeWrapper(bomNodes, projectSyncObjectBuilder);
			assertThat(testTreeWrapper, is(notNullValue()));
		}
		catch (IOException e)
		{
			fail(e);
		}
	}
}
