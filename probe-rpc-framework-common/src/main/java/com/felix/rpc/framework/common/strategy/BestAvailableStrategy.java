package com.felix.rpc.framework.common.strategy;

import java.util.ArrayList;
import java.util.List;

import org.apache.curator.x.discovery.ProviderStrategy;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.InstanceProvider;

import com.felix.rpc.framework.common.config.SelectStrategy;
import com.felix.rpc.framework.common.utils.LoadBalancerUtil;
import com.netflix.loadbalancer.Server;

public class BestAvailableStrategy<T> implements ProviderStrategy<T> {

	@Override
	public ServiceInstance<T> getInstance(InstanceProvider<T> instanceProvider) throws Exception {
		List<ServiceInstance<T>> instances = instanceProvider.getInstances();
		List<String> hosts = new ArrayList<>();
		for (ServiceInstance<T> instance : instances) {
			hosts.add(instance.getAddress() + ":" + instance.getPort());
		}
		Server selectServer = LoadBalancerUtil.selectServer(hosts, SelectStrategy.BESTAVAILABLE);
		for (ServiceInstance<T> instance : instances) {
			if ((instance.getAddress() + ":" + instance.getPort()).equals(selectServer.getHostPort())) {
				return instance;
			}
		}
		return null;
	}
}
