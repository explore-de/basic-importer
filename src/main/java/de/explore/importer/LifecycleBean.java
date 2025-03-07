package de.explore.importer;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.apache.avro.Conversions;
import org.apache.avro.specific.SpecificData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		SpecificData.get().addLogicalTypeConversion(new Conversions.DecimalConversion());
		LOG.info("The basic importer is starting...");
	}
}
