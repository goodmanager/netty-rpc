package com.felix.rpc.framework.common.utils;

import java.util.ArrayList;
import java.util.List;

import com.felix.rpc.framework.common.config.SelectStrategy;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.BestAvailableRule;
import com.netflix.loadbalancer.RandomRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.WeightedResponseTimeRule;

public class LoadBalancerUtil {
	/**
	 * 从服务器列表中选择一个
	 * 
	 * @param hosts
	 * @return
	 */
	public static Server selectServer(List<String> hosts, SelectStrategy selectStrategy) {

		BaseLoadBalancer lb = new BaseLoadBalancer();
		List<Server> servers = new ArrayList<>();
		for (String host : hosts) {
			String[] address = host.split(":");
			servers.add(new Server(address[0], Integer.valueOf(address[1])));
		}
		lb.addServers(servers);
		if (selectStrategy.getIndex() == SelectStrategy.RANDOM.getIndex()) {
			RandomRule randomRule = new RandomRule();
			lb.setRule(randomRule);
		} else if (selectStrategy.getIndex() == SelectStrategy.BESTAVAILABLE.getIndex()) {
			BestAvailableRule bestAvailable = new BestAvailableRule();
			lb.setRule(bestAvailable);
		} else if (selectStrategy.getIndex() == SelectStrategy.WEIGHTEDRESPONSETIME.getIndex()) {
			WeightedResponseTimeRule weightedResponseTimeRule = new WeightedResponseTimeRule();
			lb.setRule(weightedResponseTimeRule);
		}
		return lb.chooseServer(null);
	}
}
