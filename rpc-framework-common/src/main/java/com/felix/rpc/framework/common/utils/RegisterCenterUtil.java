package com.felix.rpc.framework.common.utils;

import java.util.ArrayList;
import java.util.List;

import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.Server;
import com.orbitz.consul.Consul;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.felix.rpc.framework.common.config.RegisterCenterConfig;
import com.google.common.net.HostAndPort;

public class RegisterCenterUtil {

	/**
	 * 从zookeeper服务器列表中选择一个
	 * 
	 * @param registerCenterConfig
	 * @return
	 */
	public static CuratorFramework getZkClient(RegisterCenterConfig registerCenterConfig) {
		CuratorFramework zkClient;
		List<String> hosts = registerCenterConfig.getHosts();
		Server server = getRegisterServer(hosts);
		zkClient = CuratorFrameworkFactory.newClient(server.getHost() + ":" + server.getPort(),
				new ExponentialBackoffRetry(1000, 3));
		zkClient.start();
		return zkClient;
	}

	/**
	 * 从consul服务器列表中选择一个
	 * 
	 * @param registerCenterConfig
	 * @return
	 */
	public static Consul getConsulClient(RegisterCenterConfig registerCenterConfig) {
		Consul client;
		List<String> hosts = registerCenterConfig.getHosts();
		Server server = getRegisterServer(hosts);
		HostAndPort hostAndPort = HostAndPort.fromParts(server.getHost(), server.getPort());
		client = Consul.builder().withHostAndPort(hostAndPort).build();
		return client;
	}

	/**
	 * ， 从列表中选择一个
	 * 
	 * @param hosts
	 * @return
	 */
	private static Server getRegisterServer(List<String> hosts) {
		BaseLoadBalancer lb = new BaseLoadBalancer();
		List<Server> servers = new ArrayList<>();
		for (String host : hosts) {
			String[] address = host.split(":");
			servers.add(new Server(address[0], Integer.valueOf(address[1])));
		}
		lb.addServers(servers);
		return lb.chooseServer(null);
	}
}
