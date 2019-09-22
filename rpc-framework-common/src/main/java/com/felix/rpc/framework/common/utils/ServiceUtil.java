package com.felix.rpc.framework.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.curator.x.discovery.ServiceInstance;

import com.felix.rpc.framework.common.dto.ZkServiceInstanceDetail;
import com.netflix.loadbalancer.Server;
import com.orbitz.consul.model.health.ServiceHealth;

public class ServiceUtil {

	public static Server selectZkService(Collection<ServiceInstance<ZkServiceInstanceDetail>> serviceInstances) {
		List<String> hosts = new ArrayList<>();
		for (ServiceInstance<ZkServiceInstanceDetail> serviceInstance : serviceInstances) {
			String ip = serviceInstance.getAddress();
			Integer port = serviceInstance.getPort();
			hosts.add(ip + ":" + port);
		}
		return LoadBalancerUtil.selectServer(hosts);
	}

	public static Server selectConsulService(List<ServiceHealth> serviceHealths) {
		List<String> hosts = new ArrayList<>();
		for (ServiceHealth serviceHealth : serviceHealths) {
			String ip = serviceHealth.getService().getAddress();
			Integer port = serviceHealth.getService().getPort();
			hosts.add(ip + ":" + port);
		}
		return LoadBalancerUtil.selectServer(hosts);
	}
}
