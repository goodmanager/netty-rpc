package com.felix.rpc.framework.common.utils;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.felix.rpc.framework.common.config.RegisterCenterConfig;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;

public class RegisterCenterUtil {

	public static CuratorFramework getZkClient(RegisterCenterConfig registerCenterConfig) {
		CuratorFramework zkClient = null;
		List<String> hosts = registerCenterConfig.getHosts();
		for (String host : hosts) {
			String[] address = host.split(":");
			zkClient = CuratorFrameworkFactory.newClient(address[0] + ":" + address[1],
					new ExponentialBackoffRetry(1000, 3));
			if (zkClient != null) {
				zkClient.start();
				break;
			}
		}
		return zkClient;
	}

	public static Consul getConsulClient(RegisterCenterConfig registerCenterConfig) {
		Consul consulClient = null;
		List<String> hosts = registerCenterConfig.getHosts();
		for (String host : hosts) {
			HostAndPort hostAndPort = HostAndPort.fromHost(host);
			consulClient = Consul.builder().withHostAndPort(hostAndPort).build();
			if (consulClient != null) {
				break;
			}
		}
		return consulClient;
	}
}
