package com.felix.rpc.framework.netty;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.orbitz.consul.Consul;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.felix.rpc.framework.common.annotation.RpcService;
import com.felix.rpc.framework.common.codec.RpcDecoder;
import com.felix.rpc.framework.common.codec.RpcEncoder;
import com.felix.rpc.framework.common.config.NettyServerConfig;
import com.felix.rpc.framework.common.config.RegisterCenterConfig;
import com.felix.rpc.framework.common.config.RegisterCenterType;
import com.felix.rpc.framework.common.dto.ConsulServiceInstanceDetail;
import com.felix.rpc.framework.common.dto.RpcRequest;
import com.felix.rpc.framework.common.dto.RpcResponse;
import com.felix.rpc.framework.common.dto.ZkServiceInstanceDetail;
import com.felix.rpc.framework.common.utils.RegisterCenterUtil;
import com.felix.rpc.framework.register.ConsulServiceRegister;
import com.felix.rpc.framework.register.ZkServiceRegister;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

@Component
public class NettyRpcServer implements ApplicationRunner {

	private Logger logger = LoggerFactory.getLogger(NettyRpcServer.class);

	// 用来保存用户服务实现类对象，key为实现类的接口名称，value为实现类对象
	private Map<String, Object> serviceBeanMap = new ConcurrentHashMap<>();

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private RegisterCenterConfig registerCenterConfig;

	@Autowired
	private NettyServerConfig nettyServerConfig;

	private ZkServiceRegister zkServiceRegister;

	private CuratorFramework client = null;

	private ConsulServiceRegister consulServiceRegister;

	private Consul consulClient = null;

	private static ThreadPoolExecutor threadPoolExecutor;

	public NettyRpcServer() {

	}

	/**
	 * 由于本类实现了InitializingBean接口，spring在构造完所有对象之后会调用afterPropertiesSet方法
	 * 在该方法中，将服务注册到zookeeper，同时启动netty服务端程序，该方法中主要是netty框架的代码
	 */
	public void startNettyServer() throws Exception {
		// 配置注册中心
		configRegisterCenter();
		// 扫描 RpcService 注解
		scanRpcService();

		logger.info("准备构建Rpc服务端，监听来自Rpc客户端的请求");
		// 配置服务端NIO线程组
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 1024)
					.childOption(ChannelOption.SO_KEEPALIVE, true)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel sc) throws Exception {
							// 添加编码器，Rpc服务端需要解码的是RpcRequest对象，因为需要接收客户端发送过来的请求
							sc.pipeline().addLast(new RpcDecoder(RpcRequest.class))
									// 添加解码器
									.addLast(new RpcEncoder(RpcResponse.class))
									// 添加业务处理handler
									.addLast(new RpcServerHandler(serviceBeanMap));
						}
					});

			// 绑定端口，同步等待成功，该方法是同步阻塞的，绑定成功后返回一个ChannelFuture
			logger.info("准备绑定服务提供者地址和端口{}:{}", nettyServerConfig.getHostName(), nettyServerConfig.getPort());
			ChannelFuture f = b.bind(nettyServerConfig.getHostName(), nettyServerConfig.getPort()).sync();

			for (String interfaceName : serviceBeanMap.keySet()) {
				registerService(interfaceName);
			}
			// 等待服务端监听端口关闭，阻塞，等待服务端链路关闭之后main函数才退出
			f.channel().closeFuture().sync();
		} finally {
			// 优雅退出，释放线程池资源
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	/**
	 * 由于本类实现了ApplicationContextAware接口，spring在构造本类对象时会调用setApplicationContext方法
	 * 在该方法中，通过注解获取标注了RPCService的用户服务实现类，然后将其接口和实现类对象保存到serviceBeanMap中
	 */
	public void scanRpcService() throws BeansException {
		logger.info("准备扫描获取标注了RpcService的用户服务实现类");
		// 通过spring获取到标注了RPCService注解的map，map的key为bean的名称，map的value为bean的实例对象
		Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(RpcService.class);
		logger.info("标注了RpcService的用户服务实现类扫描完毕，准备将其接口和实现类对象保存到容器中");
		for (Object serviceBean : beansWithAnnotation.values()) {
			/**
			 * 获取实现类对象的接口名称，思路是，实现类中标注了RPCService注解，同时其参数为实现类的接口类型
			 * 说明：实际上可以通过serviceBean.getClass().getInterfaces()的方式来获取其接口名称的
			 * 只是如果是实现多个接口的情况下需要进行判断，这点后面再做具体的实现
			 */
			String interfaceName = serviceBean.getClass().getAnnotation(RpcService.class).targetInterface().getName();
			// 保存到serviceBeanMap中
			serviceBeanMap.put(interfaceName, serviceBean);
		}
		logger.info("扫描完毕,总共扫描的RpcService:{}", serviceBeanMap.keySet());
	}

	public static void submit(Runnable task) {
		if (threadPoolExecutor == null) {
			synchronized (NettyRpcServer.class) {
				if (threadPoolExecutor == null) {
					int threadCount = Runtime.getRuntime().availableProcessors();
					threadPoolExecutor = new ThreadPoolExecutor(threadCount, threadCount * 10, 500L, TimeUnit.MILLISECONDS,
							new ArrayBlockingQueue<Runnable>(20480));
				}
			}
		}
		threadPoolExecutor.submit(task);
	}

	private void registerService(String interfaceName) throws Exception {
		String id = nettyServerConfig.getHostName() + "-" + nettyServerConfig.getPort() + "-" + interfaceName;
		int index = registerCenterConfig.getRegisterCenterType().getIndex();
		if (index == RegisterCenterType.ZOOKEEPER.getIndex()) {
			// 注册service 实例到zookeeper
			ServiceInstance<ZkServiceInstanceDetail> serviceInstance = ServiceInstance
					.<ZkServiceInstanceDetail>builder().id(id).name(interfaceName).port(nettyServerConfig.getPort())
					.address(nettyServerConfig.getHostName())
					.payload(new ZkServiceInstanceDetail(id, nettyServerConfig.getHostName(),
							nettyServerConfig.getPort(), interfaceName))
					.uriSpec(new UriSpec("{scheme}://{address}:{port}")).build();

			zkServiceRegister.registerService(serviceInstance);
		} else if (index == RegisterCenterType.CONSUL.getIndex()) {
			// 注册service 实例到 consul
			ConsulServiceInstanceDetail consulServiceInstanceDetail = new ConsulServiceInstanceDetail();
			consulServiceInstanceDetail.setId(id);
			consulServiceInstanceDetail.setHostName(nettyServerConfig.getHostName());
			consulServiceInstanceDetail.setListenPort(nettyServerConfig.getPort());
			consulServiceInstanceDetail.setInterfaceName(interfaceName);
			consulServiceRegister.registerService(consulServiceInstanceDetail);
		}
		logger.info("向注册中心:{},注册服务:{}成功,正在监听来自RpcClient的请求连接",
				registerCenterConfig.getRegisterCenterType().getDescription(), interfaceName);
	}

	/**
	 * 注册中心
	 *
	 * @throws Exception
	 */
	private void configRegisterCenter() throws Exception {
		int index = registerCenterConfig.getRegisterCenterType().getIndex();
		// 创建zookeeper client
		if (index == RegisterCenterType.ZOOKEEPER.getIndex()) {
			client = RegisterCenterUtil.getZkClient(registerCenterConfig);
			zkServiceRegister = new ZkServiceRegister(client, registerCenterConfig.getBasePath());
			zkServiceRegister.start();
		} else if (index == RegisterCenterType.CONSUL.getIndex()) {
			// 创建consul client
			consulClient = RegisterCenterUtil.getConsulClient(registerCenterConfig);
			consulServiceRegister = new ConsulServiceRegister(consulClient);
		}
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		startNettyServer();
	}

}
