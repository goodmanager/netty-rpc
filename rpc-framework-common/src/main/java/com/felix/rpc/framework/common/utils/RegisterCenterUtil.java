package com.felix.rpc.framework.common.utils;

import java.util.List;

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
		Server server = LoadBalancerUtil.selectServer(hosts);
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
		Server server = LoadBalancerUtil.selectServer(hosts);
		HostAndPort hostAndPort = HostAndPort.fromParts(server.getHost(), server.getPort());
		client = Consul.builder().withHostAndPort(hostAndPort).build();
		return client;
	}

}
