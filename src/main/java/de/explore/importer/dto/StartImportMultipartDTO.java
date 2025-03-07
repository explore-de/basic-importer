package de.explore.importer.dto;

import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;

import java.io.InputStream;

public class StartImportMultipartDTO
{
	@RestForm("projectName")
	@PartType(MediaType.TEXT_PLAIN)
	String projectName;

	@RestForm("zip")
	@PartType(MediaType.APPLICATION_OCTET_STREAM)
	InputStream zipFile;

	public String getProjectName()
	{
		return projectName;
	}

	public InputStream getZipFile()
	{
		return zipFile;
	}
}
