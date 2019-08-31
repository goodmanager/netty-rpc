package com.felix.rpc.framework.client.discovery;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.HealthServicesRequest;
import com.ecwid.consul.v1.health.model.HealthService;

@Component
public class ConsulServiceDiscovery {

	private Logger logger = LoggerFactory.getLogger(ConsulServiceDiscovery.class);

	private final ConcurrentHashMap<String, Response<List<HealthService>>> servicesInstanceMap = new ConcurrentHashMap<>();

	private ConsulClient client;

	public ConsulServiceDiscovery() {

	}

	public ConsulServiceDiscovery(ConsulClient consulClient) {
		this.client = consulClient;
	}

	public Response<List<HealthService>> findServices(String serviceName) {

		Response<List<HealthService>> healthyServices = servicesInstanceMap.get(serviceName);
		if (healthyServices == null) {
			HealthServicesRequest request = HealthServicesRequest.newBuilder().setPassing(true)
					.setQueryParams(QueryParams.DEFAULT).build();

			healthyServices = client.getHealthServices(serviceName, request);
			servicesInstanceMap.put(serviceName, healthyServices);

			logger.info("healthy services:{}", healthyServices);
		}
		return healthyServices;
	}

}
