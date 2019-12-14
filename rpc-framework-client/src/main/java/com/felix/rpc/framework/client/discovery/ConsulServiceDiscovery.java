package com.felix.rpc.framework.client.discovery;

import java.util.List;

import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.model.health.ServiceHealth;

import org.springframework.stereotype.Component;

@Component
public class ConsulServiceDiscovery {

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
