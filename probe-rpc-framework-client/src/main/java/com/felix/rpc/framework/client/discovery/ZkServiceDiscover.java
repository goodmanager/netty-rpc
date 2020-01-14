package com.felix.rpc.framework.client.discovery;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.felix.rpc.framework.common.config.NettyServerConfig;
import com.felix.rpc.framework.common.dto.RpcRequest;
import com.felix.rpc.framework.common.dto.ZkServiceInstanceDetail;
import com.felix.rpc.framework.common.utils.LoadBalancerUtil;

@Component
public class ZkServiceDiscover {
	private Logger logger = LoggerFactory.getLogger(ZkServiceDiscover.class);

	private ServiceDiscovery<ZkServiceInstanceDetail> serviceDiscovery;

	private NettyServerConfig nettyServerConfig;

	private final ConcurrentHashMap<String, ServiceProvider<ZkServiceInstanceDetail>> serviceProviderMap = new ConcurrentHashMap<>();

	public ZkServiceDiscover() {

	}

	public ZkServiceDiscover(CuratorFramework client, String basePath, NettyServerConfig nettyServerConfig) {
		serviceDiscovery = ServiceDiscoveryBuilder.builder(ZkServiceInstanceDetail.class).client(client)
				.basePath(basePath).serializer(new JsonInstanceSerializer<>(ZkServiceInstanceDetail.class)).build();
		this.nettyServerConfig = nettyServerConfig;
	}

	/**
	 * 选择一个服务所在的服务器
	 * 
	 * @param interfaceName
	 * @return
	 * @throws Exception
	 */
	public ServiceInstance<ZkServiceInstanceDetail> getServiceInstance(RpcRequest rpcRequest) throws Exception {
		ServiceProvider<ZkServiceInstanceDetail> provider = serviceProviderMap.get(rpcRequest.getInterfaceName());
		if (provider == null) {
			provider = LoadBalancerUtil.selectZookeeperServer(serviceDiscovery, rpcRequest.getInterfaceName(),
					serviceProviderMap, nettyServerConfig);
		}
		ServiceInstance<ZkServiceInstanceDetail> instance = provider.getInstance();
		logger.info("requestId:{},选择了服务{}", rpcRequest.getRequestId(), instance.getId());
		return instance;
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
