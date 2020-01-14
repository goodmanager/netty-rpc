package com.felix.rpc.framework.client.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.orbitz.consul.Consul;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.felix.rpc.framework.client.discovery.ConsulServiceDiscovery;
import com.felix.rpc.framework.client.discovery.ZkServiceDiscover;
import com.felix.rpc.framework.client.netty.RpcClient;
import com.felix.rpc.framework.common.config.NettyServerConfig;
import com.felix.rpc.framework.common.config.RegisterCenterConfig;
import com.felix.rpc.framework.common.config.RegisterCenterType;
import com.felix.rpc.framework.common.dto.RpcRequest;
import com.felix.rpc.framework.common.dto.RpcResponse;
import com.felix.rpc.framework.common.dto.ZkServiceInstanceDetail;
import com.felix.rpc.framework.common.utils.RegisterCenterUtil;
import com.netflix.loadbalancer.Server;

import org.springframework.stereotype.Component;

/**
 * 利用代理优化远程调用 使之像本地调用一样 动态代理对象类，用于根据接口创建动态代理对象
 */
@Component
public class RpcProxy {

	private Logger logger = LoggerFactory.getLogger(RpcProxy.class);

	@Autowired
	private RegisterCenterConfig registerCenterConfig;

	@Autowired
	private NettyServerConfig nettyServerConfig;

	/**
	 * 获得动态代理对象的通用方法，实现思路：该方法中，并不需要具体的实现类对象。因为在invoke方法中，并不会调用Method这个方法
	 * 只是获取其方法的名字，然后将其封装在netty请求中，发送到metty服务端中请求远程调用的结果
	 *
	 * @param interfaceClass 需要被代理的接口的类型对象
	 * @param <T>            对应接口的代理对象
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getProxy(Class<?> interfaceClass, String requestId) {

		T proxy = (T) Proxy.newProxyInstance(RpcProxy.class.getClassLoader(), new Class<?>[] { interfaceClass },
				new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						logger.info("requestId:{},准备构建RpcRequest对象", requestId);
						// 构建RpcRequest
						RpcRequest rpcRequest = new RpcRequest();
						// 设置requestId
						rpcRequest.setRequestId(requestId);
						// 设置接口
						String interfaceName = method.getDeclaringClass().getName();
						rpcRequest.setInterfaceName(interfaceName);
						rpcRequest.setMethodName(method.getName());
						rpcRequest.setParameterTypes(method.getParameterTypes());
						// 设置参数列表parameters
						rpcRequest.setParameters(args);

						logger.info("requestId:{},RpcRequest:{},构建完毕", requestId, rpcRequest);
						RpcClient rpcClient = null;
						// 发现服务，得到服务地址，格式为 host:port
						if (registerCenterConfig.getRegisterCenterType().getIndex() == RegisterCenterType.ZOOKEEPER
								.getIndex()) {
							// 从zookeeper注册中心选择一台注册中心服务器
							CuratorFramework zkClient = RegisterCenterUtil.getZkClient(registerCenterConfig, requestId);
							ZkServiceDiscover zkServiceDiscover = new ZkServiceDiscover(zkClient,
									registerCenterConfig.getBasePath(), nettyServerConfig);
							// 选择一个服务
							ServiceInstance<ZkServiceInstanceDetail> serviceInstance = zkServiceDiscover
									.getServiceInstance(rpcRequest);
							// 如果服务不存在，null,否则就构建rpc客户端进行远程调用
							if (serviceInstance == null) {
								logger.error("requestId:{},服务:{}的提供者不存在,发现服务失败", requestId, rpcRequest);
								return null;
							} else {
								// 解析服务地址
								logger.info("requestId:{},服务地址解析完毕,准备构建RpcClient", requestId);
								// 构建rpc客户端
								rpcClient = new RpcClient(serviceInstance.getAddress(), serviceInstance.getPort());
							}
						} else {
							// 从consul注册中心选择一台注册中心服务器
							Consul consulClient = RegisterCenterUtil.getConsulClient(registerCenterConfig, requestId);
							ConsulServiceDiscovery consulServiceDiscovery = new ConsulServiceDiscovery(consulClient,
									nettyServerConfig);
							// 选择一个服务
							Server service = consulServiceDiscovery.getServiceInstance(rpcRequest);
							// 如果服务不存在，null,否则就构建rpc客户端进行远程调用
							if (service == null) {
								logger.error("requestId:{},服务:{}的提供者不存在,发现服务失败", requestId, rpcRequest);
								return null;
							} else {
								// 解析服务地址
								logger.info("requestId:{},服务地址解析完毕,准备构建RpcClient", requestId);
								// 构建rpc客户端
								rpcClient = new RpcClient(service.getHost(), service.getPort());
							}
						}
						logger.info("requestId:{},RpcClient构建完毕,准备向Rpc服务端发送请求,请求参数:{}", requestId, rpcClient);
						// 向rpc服务端发送请求,返回信息
						RpcResponse rpcResponse = rpcClient.sendRequest(rpcRequest);
						if (rpcResponse.getError() != null) {
							logger.error("requestId:{},请求失败:{}", requestId, rpcResponse.getError());
							throw rpcResponse.getError();
						} else {
							// 如果没有异常，则返回调用的结果
							Object result = rpcResponse.getResult();
							logger.info("requestId:{},{}远程过程调用完毕,远程过程调用成功,返回数据:{}", requestId, interfaceName, result);
							return result;
						}
					}
				});
		return proxy;
	}

}
