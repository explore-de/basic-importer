package de.exentra.explore.basic.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.exentra.explore.basic.importer.gson.GsonExclusionStrategy;
import de.exentra.explore.basic.importer.importer.JsonTreeBuilder;
import de.exentra.explore.basic.importer.pojo.Node;
import io.micrometer.core.annotation.Timed;

@Path("/import")
public class ImporterResource
{
	private static final Logger LOG = LoggerFactory.getLogger(ImporterResource.class);

	@Inject
	Provider<JsonTreeBuilder> jsonTreeBuilderProvider;

	@POST
	@Timed
	@Path("/vehicle")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response startImport(@RestForm @PartType(MediaType.APPLICATION_OCTET_STREAM) File file) throws Exception
	{
		LOG.info("Setup...");

		String jsonTxt = getJsonString(file);

		Gson gson = new GsonBuilder().setExclusionStrategies(new GsonExclusionStrategy()).create();

		LOG.info("Setup done");

		Node[] list = gson.fromJson(jsonTxt, Node[].class);
		jsonTreeBuilderProvider.get().buildTree(Arrays.asList(list));

		return Response.accepted().build();
	}

	private static String getJsonString(File file) throws IOException
	{
		String jsonTxt;
		try (InputStream inputStream = new FileInputStream(file.getAbsolutePath()))
		{
			jsonTxt = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
		}
		return jsonTxt;
	}
}
