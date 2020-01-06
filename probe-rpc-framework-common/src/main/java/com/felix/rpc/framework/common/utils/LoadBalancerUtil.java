package com.felix.rpc.framework.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.strategies.RandomStrategy;
import org.apache.curator.x.discovery.strategies.RoundRobinStrategy;

import com.felix.rpc.framework.common.config.NettyServerConfig;
import com.felix.rpc.framework.common.config.SelectStrategy;
import com.felix.rpc.framework.common.dto.ZkServiceInstanceDetail;
import com.felix.rpc.framework.common.strategy.BestAvailableStrategy;
import com.felix.rpc.framework.common.strategy.WeightedResponseTimeStrategy;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.BestAvailableRule;
import com.netflix.loadbalancer.RandomRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.WeightedResponseTimeRule;

public class LoadBalancerUtil {

	private static BaseLoadBalancer lb;
	static {
		lb = new BaseLoadBalancer();
	}

	/**
	 * 从服务器列表中选择一个
	 * 
	 * @param hosts
	 * @return
	 */
	public static Server selectServer(List<String> hosts, SelectStrategy selectStrategy) {
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
		List<Server> servers = new ArrayList<>();
		for (String host : hosts) {
			String[] address = host.split(":");
			servers.add(new Server(address[0], Integer.valueOf(address[1])));
		}
		lb.addServers(servers);
		return lb.chooseServer(null);
	}

	public static Server selectConsulServer(List<String> hosts, SelectStrategy selectStrategy) {
		return selectServer(hosts, selectStrategy);
	}

	public static ServiceProvider<ZkServiceInstanceDetail> selectZookeeperServer(
			ServiceDiscovery<ZkServiceInstanceDetail> serviceDiscovery, String interfaceName,
			ConcurrentHashMap<String, ServiceProvider<ZkServiceInstanceDetail>> serviceProviderMap,
			NettyServerConfig nettyServerConfig) throws Exception {
		ServiceProvider<ZkServiceInstanceDetail> provider = null;
		int index = nettyServerConfig.getSelectStrategy().getIndex();
		if (index == SelectStrategy.RANDOM.getIndex()) {
			provider = serviceDiscovery.serviceProviderBuilder().serviceName(interfaceName)
					.providerStrategy(new RandomStrategy<ZkServiceInstanceDetail>()).build();
		} else if (index == SelectStrategy.BESTAVAILABLE.getIndex()) {
			provider = serviceDiscovery.serviceProviderBuilder().serviceName(interfaceName)
					.providerStrategy(new BestAvailableStrategy<ZkServiceInstanceDetail>()).build();
		} else if (index == SelectStrategy.WEIGHTEDRESPONSETIME.getIndex()) {
			provider = serviceDiscovery.serviceProviderBuilder().serviceName(interfaceName)
					.providerStrategy(new WeightedResponseTimeStrategy<ZkServiceInstanceDetail>()).build();
		} else {
			provider = serviceDiscovery.serviceProviderBuilder().serviceName(interfaceName)
					.providerStrategy(new RoundRobinStrategy<ZkServiceInstanceDetail>()).build();
		}
		ServiceProvider<ZkServiceInstanceDetail> oldProvider = serviceProviderMap.putIfAbsent(interfaceName, provider);
		if (oldProvider != null) {
			provider = oldProvider;
		} else {
			provider.start();
		}
		return provider;
	}
}
