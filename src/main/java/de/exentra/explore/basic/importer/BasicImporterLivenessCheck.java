package de.exentra.explore.basic.importer;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

@Liveness
public class BasicImporterLivenessCheck implements HealthCheck
{
	@Override
	public HealthCheckResponse call()
	{
		return HealthCheckResponse.up("alive");
	}
}