package com.felix.rpc.framework.common.utils;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.felix.rpc.framework.common.config.RegisterCenterConfig;
import com.felix.rpc.framework.common.exception.RpcFrameworkException;
import com.google.common.net.HostAndPort;
import com.netflix.loadbalancer.Server;
import com.orbitz.consul.Consul;

public class RegisterCenterUtil {

	private static final Logger logger = LoggerFactory.getLogger(RegisterCenterUtil.class);

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
		if (server == null) {
			throw new RpcFrameworkException("获取zookeeper client失败");
		}
		zkClient = CuratorFrameworkFactory.newClient(server.getHostPort(), new ExponentialBackoffRetry(200, 3));
		zkClient.start();
		logger.info("选择了zookeeper注册中心的:{}服务器", server.getHostPort());
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
		if (server == null) {
			throw new RpcFrameworkException("获取consul client失败");
		}
		HostAndPort hostAndPort = HostAndPort.fromString(server.getHostPort());
		logger.info("选择了consul注册中心的:{}服务器", server.getHostPort());
		return Consul.builder().withHostAndPort(hostAndPort).build();
	}
}
