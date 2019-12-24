package com.felix.rpc.framework.register;

import java.io.IOException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import com.felix.rpc.framework.common.dto.ZkServiceInstanceDetail;

public class ZkServiceRegister {

	private ServiceDiscovery<ZkServiceInstanceDetail> serviceDiscovery;

	public ZkServiceRegister(CuratorFramework client, String basePath) throws Exception {
		JsonInstanceSerializer<ZkServiceInstanceDetail> serializer = new JsonInstanceSerializer<ZkServiceInstanceDetail>(
				ZkServiceInstanceDetail.class);
		serviceDiscovery = ServiceDiscoveryBuilder.builder(ZkServiceInstanceDetail.class).client(client)
				.serializer(serializer).basePath(basePath).build();
	}

	public void registerService(ServiceInstance<ZkServiceInstanceDetail> serviceInstance) throws Exception {
		serviceDiscovery.registerService(serviceInstance);
	}

	public void unregisterService(ServiceInstance<ZkServiceInstanceDetail> serviceInstance) throws Exception {
		serviceDiscovery.unregisterService(serviceInstance);

	}

	public void updateService(ServiceInstance<ZkServiceInstanceDetail> serviceInstance) throws Exception {
		serviceDiscovery.updateService(serviceInstance);

	}

	public void start() throws Exception {
		serviceDiscovery.start();
	}

	public void close() throws IOException {
		serviceDiscovery.close();
	}

}
