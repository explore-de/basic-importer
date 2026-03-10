package de.explore.importer.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import jakarta.inject.Inject;

import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.Test;

@QuarkusTest
class ZipServiceTest
{
	@Inject
	ZipService zipService;

	@Test
	void shouldReadZipFileAndReturnMap()
	{
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("archive.zip"))
		{
			Map<String, byte[]> extracted = zipService.extractZipFileFromInputStream(inputStream);

			assertThat(extracted, is(notNullValue()));
			assertThat(extracted, is(not(anEmptyMap())));
			extracted.forEach((s, bytes) -> {
				assertThat(bytes, is(notNullValue()));
				assertThat(s, is(not(blankOrNullString())));
			});
		}
		catch (IOException e)
		{
			fail(e);
		}
	}

	@Test
	void shouldReturnEmptyMapForInvalidZipFile()
	{
		byte[] invalidZipData = "This is not a valid zip file".getBytes(StandardCharsets.UTF_8);
		try (InputStream inputStream = new ByteArrayInputStream(invalidZipData))
		{
			Map<String, byte[]> extracted = zipService.extractZipFileFromInputStream(inputStream);
			// Assert that the map is not null but is empty.
			assertThat(extracted, is(notNullValue()));
			assertThat(extracted, is(anEmptyMap()));
		}
		catch (IOException e)
		{
			fail(e);
		}
	}
}
