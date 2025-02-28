package de.explore.importer.service;

import de.explore.importer.model.BomNode;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
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
class CsvServiceTest
{
	@Inject
	CsvService csvService;

	@BeforeEach
	void setUp()
	{
	}

	@AfterEach
	void tearDown()
	{
	}

	@Test
	void shouldParseCSV()
	{
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("train-bom.csv"))
		{
			byte[] csvBytes = inputStream.readAllBytes();
			List<BomNode> bomNodes = csvService.parseCSV(csvBytes);

			assertThat(bomNodes, is(not(emptyIterable())));
			assertThat(bomNodes.size(), is(1));

			BomNode bomNode = bomNodes.getFirst();
			assertThat(bomNode, is(notNullValue()));
			assertThat(bomNode.getChildren(), is(not(emptyIterable())));
		}
		catch (IOException e)
		{
			fail(e);
		}
	}
}
