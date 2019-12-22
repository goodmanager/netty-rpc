package com.felix.rpc.framework.common.utils;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.felix.rpc.framework.common.config.RegisterCenterConfig;
import com.google.common.net.HostAndPort;
import com.netflix.loadbalancer.Server;
import com.orbitz.consul.Consul;

public class RegisterCenterUtil {

	public static CuratorFramework getZkClient(RegisterCenterConfig registerCenterConfig) {
		CuratorFramework zkClient;
		List<String> hosts = registerCenterConfig.getHosts();
		Server server = LoadBalancerUtil.selectServer(hosts, registerCenterConfig.getSelectStrategy());
		zkClient = CuratorFrameworkFactory.newClient(server.getHost() + ":" + server.getPort(),
				new ExponentialBackoffRetry(1000, 3));
		zkClient.start();
		return zkClient;
	}

	public static Consul getConsulClient(RegisterCenterConfig registerCenterConfig) {
		List<String> hosts = registerCenterConfig.getHosts();
		Server server = LoadBalancerUtil.selectServer(hosts, registerCenterConfig.getSelectStrategy());
		HostAndPort hostAndPort = HostAndPort.fromHost(server.getHost());
		return Consul.builder().withHostAndPort(hostAndPort).build();
	}
}
