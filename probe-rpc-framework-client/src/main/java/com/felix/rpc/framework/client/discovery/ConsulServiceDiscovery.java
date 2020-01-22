package com.felix.rpc.framework.client.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.felix.rpc.framework.common.config.NettyServerConfig;
import com.felix.rpc.framework.common.dto.RpcRequest;
import com.felix.rpc.framework.common.utils.LoadBalancerUtil;
import com.netflix.loadbalancer.Server;
import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.model.health.ServiceHealth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ConsulServiceDiscovery {

	private static final Logger logger = LoggerFactory.getLogger(ConsulServiceDiscovery.class);

	private Consul client;

	private NettyServerConfig nettyServerConfig;

	private Map<String, Integer> hosts = new ConcurrentHashMap<>();

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
	public Server getServiceInstance(RpcRequest rpcRequest) {
		HealthClient healthClient = client.healthClient();
		List<ServiceHealth> nodes = healthClient.getHealthyServiceInstances(rpcRequest.getInterfaceName())
				.getResponse();
		List<String> hosts = new ArrayList<>();
		for (ServiceHealth serviceHealth : nodes) {
			hosts.add(serviceHealth.getService().getAddress() + ":" + serviceHealth.getService().getPort());
		}
		Server selectConsulServer = LoadBalancerUtil.selectConsulServer(hosts, nettyServerConfig.getSelectStrategy());
		logger.info("选择了服务:{}", selectConsulServer.getId());
		return selectConsulServer;
	}
}
