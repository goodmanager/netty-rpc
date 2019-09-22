package com.felix.rpc.framework.common.utils;

import java.util.ArrayList;
import java.util.List;

import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.Server;

public class LoadBalancerUtil {
	/**
	 * 从列表中选择一个
	 * 
	 * @param hosts
	 * @return
	 */
	public static Server selectServer(List<String> hosts) {
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
