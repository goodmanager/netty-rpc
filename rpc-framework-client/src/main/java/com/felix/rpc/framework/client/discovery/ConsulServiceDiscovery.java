package com.felix.rpc.framework.client.discovery;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.model.health.ServiceHealth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ConsulServiceDiscovery {

	private Logger logger = LoggerFactory.getLogger(ConsulServiceDiscovery.class);

	private final ConcurrentHashMap<String, List<ServiceHealth>> servicesInstanceMap = new ConcurrentHashMap<>();

	private Consul client;

	public ConsulServiceDiscovery() {

	}

	public ConsulServiceDiscovery(Consul client) {
		this.client = client;
	}

	public ServiceHealth getServiceInstance(String serviceName) {
		HealthClient healthClient = client.healthClient();
		List<ServiceHealth> nodes = healthClient.getHealthyServiceInstances(serviceName).getResponse();
		return nodes.get(0);
	}

	public List<ServiceHealth> getServiceInstances(String serviceName) {
		HealthClient healthClient = client.healthClient();
		List<ServiceHealth> nodes = healthClient.getHealthyServiceInstances(serviceName).getResponse();
		return nodes;
	}

}
