package de.explore.importer.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.InternalServerErrorException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@ApplicationScoped
public class ZipService
{
	public Map<String, byte[]> extractZipFileFromInputStream(InputStream inputStream)
	{
		Map<String, byte[]> extractedFiles = new HashMap<>();
		try (ZipInputStream zipInputStream = new ZipInputStream(inputStream))
		{
			ZipEntry entry;
			while ((entry = zipInputStream.getNextEntry()) != null)
			{
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				zipInputStream.transferTo(bos);
				extractedFiles.put(entry.getName(), bos.toByteArray());
			}
		}
		catch (IOException e)
		{
			throw new InternalServerErrorException(e.getMessage());
		}
		return extractedFiles;
	}
}
