package de.exentra.explore.basic.importer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class LifecycleBean
{
	private static final Logger LOG = LoggerFactory.getLogger(LifecycleBean.class);

	void onStart(@Observes StartupEvent ev)
	{
		// this system property is needed so that avro objects use their custom
		// encoder
		// we need this because we had problems with encoding objects in avro
		System.setProperty("org.apache.avro.specific.use_custom_coders", "true");
		LOG.info("The basic importer is starting...");
	}
}