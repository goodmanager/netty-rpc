package com.felix.rpc.framework.client.discovery;

import java.util.ArrayList;
import java.util.List;

import com.felix.rpc.framework.common.config.NettyServerConfig;
import com.felix.rpc.framework.common.utils.LoadBalancerUtil;
import com.netflix.loadbalancer.Server;
import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.model.health.ServiceHealth;

import org.springframework.stereotype.Component;

@Component
public class ConsulServiceDiscovery {

	private Consul client;

	private NettyServerConfig nettyServerConfig;

	public ConsulServiceDiscovery() {

	}

	public ConsulServiceDiscovery(Consul client, NettyServerConfig nettyServerConfig) {
		this.client = client;
		this.nettyServerConfig = nettyServerConfig;
	}

	/**
	 * 选择具体的服务器
	 * 
	 * @param serviceName
	 * @return
	 */
	public Server getServiceInstance(String serviceName) {
		HealthClient healthClient = client.healthClient();
		List<ServiceHealth> nodes = healthClient.getHealthyServiceInstances(serviceName).getResponse();
		List<String> hosts = new ArrayList<>();
		for (ServiceHealth serviceHealth : nodes) {
			hosts.add(serviceHealth.getService().getAddress() + ":" + serviceHealth.getService().getPort());
		}
		return LoadBalancerUtil.selectConsulServer(hosts, nettyServerConfig.getSelectStrategy());
	}
}
