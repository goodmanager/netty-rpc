package com.felix.rpc.framework.client.discovery;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.strategies.RoundRobinStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.felix.rpc.framework.common.config.NettyServerConfig;
import com.felix.rpc.framework.common.dto.ZkServiceInstanceDetail;

@Component
public class ZkServiceDiscover {
	private Logger logger = LoggerFactory.getLogger(ZkServiceDiscover.class);

	@Autowired
	private NettyServerConfig nettyServerConfig;

	private ServiceDiscovery<ZkServiceInstanceDetail> serviceDiscovery;
	private final ConcurrentHashMap<String, ServiceProvider<ZkServiceInstanceDetail>> serviceProviderMap = new ConcurrentHashMap<>();

	public ZkServiceDiscover() {

	}

	public ZkServiceDiscover(CuratorFramework client, String basePath) {
		serviceDiscovery = ServiceDiscoveryBuilder.builder(ZkServiceInstanceDetail.class).client(client)
				.basePath(basePath).serializer(new JsonInstanceSerializer<>(ZkServiceInstanceDetail.class)).build();
	}

	public ServiceInstance<ZkServiceInstanceDetail> getServiceInstance(String interfaceName) throws Exception {
		ServiceProvider<ZkServiceInstanceDetail> provider = serviceProviderMap.get(interfaceName);
		if (provider == null) {
			provider = serviceDiscovery.serviceProviderBuilder().serviceName(interfaceName)
					.providerStrategy(new RoundRobinStrategy<ZkServiceInstanceDetail>()).build();

			ServiceProvider<ZkServiceInstanceDetail> oldProvider = serviceProviderMap.putIfAbsent(interfaceName,
					provider);
			if (oldProvider != null) {
				provider = oldProvider;
			} else {
				provider.start();
			}
		}

		return provider.getInstance();
	}

	public Collection<ServiceInstance<ZkServiceInstanceDetail>> getServiceInstances(String interfaceName)
			throws Exception {
		return serviceDiscovery.queryForInstances(interfaceName);
	}

	public void start() throws Exception {
		serviceDiscovery.start();
	}

	public void close() throws IOException {

		for (Map.Entry<String, ServiceProvider<ZkServiceInstanceDetail>> me : serviceProviderMap.entrySet()) {
			try {
				me.getValue().close();
			} catch (Exception e) {
				logger.error("关闭配置中心失败", e);
			}
		}
		serviceDiscovery.close();
	}
}
