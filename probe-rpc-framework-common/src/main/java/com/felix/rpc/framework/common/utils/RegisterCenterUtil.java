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
	public static CuratorFramework getZkClient(RegisterCenterConfig registerCenterConfig, String requestId) {
		CuratorFramework zkClient;
		List<String> hosts = registerCenterConfig.getHosts();
		Server server = LoadBalancerUtil.selectServer(hosts, registerCenterConfig.getSelectStrategy());
		if (server == null) {
			String errorMsg;
			if (requestId == null) {
				errorMsg = "获取zookeeper client失败";
			} else {
				errorMsg = String.format("requestId:%s,获取zookeeper client失败", requestId);
			}
			throw new RpcFrameworkException(errorMsg);
		}
		zkClient = CuratorFrameworkFactory.newClient(server.getHostPort(), new ExponentialBackoffRetry(200, 3));
		zkClient.start();
		if (requestId != null) {
			logger.info("选择了zookeeper注册中心的:{}服务器", server.getHostPort());
		} else {
			logger.info("requestId:{},选择了zookeeper注册中心的:{}服务器", requestId, server.getHostPort());
		}
		return zkClient;
	}

	/**
	 * 从consul注册中心选择一个consul服务器
	 * 
	 * @param registerCenterConfig
	 * @return
	 */
	public static Consul getConsulClient(RegisterCenterConfig registerCenterConfig, String requestId) {
		List<String> hosts = registerCenterConfig.getHosts();
		Server server = LoadBalancerUtil.selectServer(hosts, registerCenterConfig.getSelectStrategy());
		if (server == null) {
			String errorMsg;
			if (requestId == null) {
				errorMsg = "获取consul client失败";
			} else {
				errorMsg = String.format("requestId:%s,获取consul client失败", requestId);
			}
			throw new RpcFrameworkException(errorMsg);
		}
		HostAndPort hostAndPort = HostAndPort.fromString(server.getHostPort());
		if (requestId == null) {
			logger.info("选择了consul注册中心的:{}服务器", server.getHostPort());
		} else {
			logger.info("requestId:{},选择了consul注册中心的:{}服务器", requestId, server.getHostPort());
		}
		return Consul.builder().withHostAndPort(hostAndPort).build();
	}
}
