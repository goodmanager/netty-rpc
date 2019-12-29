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

	/**
	 * 从zookeeper注册中心选择一个zookeeper服务器
	 * 
	 * @param registerCenterConfig
	 * @return
	 */
	public static CuratorFramework getZkClient(RegisterCenterConfig registerCenterConfig) {
		CuratorFramework zkClient;
		List<String> hosts = registerCenterConfig.getHosts();
		Server server = LoadBalancerUtil.selectServer(hosts, registerCenterConfig.getSelectStrategy());
		zkClient = CuratorFrameworkFactory.newClient(server.getHostPort(), new ExponentialBackoffRetry(200, 3));
		zkClient.start();
		return zkClient;
	}

	/**
	 * 从consul注册中心选择一个consul服务器
	 * 
	 * @param registerCenterConfig
	 * @return
	 */
	public static Consul getConsulClient(RegisterCenterConfig registerCenterConfig) {
		List<String> hosts = registerCenterConfig.getHosts();
		Server server = LoadBalancerUtil.selectServer(hosts, registerCenterConfig.getSelectStrategy());
		HostAndPort hostAndPort = HostAndPort.fromString(server.getHostPort());
		return Consul.builder().withHostAndPort(hostAndPort).build();
	}
}
