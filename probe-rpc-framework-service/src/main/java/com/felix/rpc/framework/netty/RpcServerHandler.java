package com.felix.rpc.framework.netty;

import java.lang.reflect.Method;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.felix.rpc.framework.common.dto.RpcRequest;
import com.felix.rpc.framework.common.dto.RpcResponse;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class RpcServerHandler extends ChannelInboundHandlerAdapter {

	private Logger logger = LoggerFactory.getLogger(RpcServerHandler.class);

	Map<String, Object> serviceBeanMap = null;

	public RpcServerHandler(Map<String, Object> serviceBeanMap) {
		this.serviceBeanMap = serviceBeanMap;
	}

	/**
	 * 接收消息，处理消息，返回结果
	 *
	 * @param ctx
	 * @param msg
	 * @throws Exception
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		NettyRpcServer.submit(new Runnable() {
			@Override
			public void run() {
				RpcRequest rpcRequest = (RpcRequest) msg;
				String requestId = rpcRequest.getRequestId();
				logger.info("requestId:{},接收到来自RPC客户端的连接请求", requestId, rpcRequest);
				RpcResponse rpcResponse = new RpcResponse();
				// 设置requestId
				rpcResponse.setRequestId(rpcRequest.getRequestId());
				try {
					// 调用handle方法处理request
					Object result = handleReuqest(rpcRequest);
					// 设置返回结果
					rpcResponse.setResult(result);
				} catch (Throwable e) {
					// 如果有异常，则设置异常信息
					rpcResponse.setError(e);
				}
				ctx.writeAndFlush(rpcResponse).addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture channelFuture) throws Exception {
						logger.info("requestId:{},请求处理完毕，回写response对象给客户端", requestId, rpcResponse);
					}
				});
			}
		});

	}

	/**
	 * 对request进行处理，其实就是通过反射进行调用的过程
	 *
	 * @param rpcRequest
	 * @return
	 * @throws Throwable
	 */
	public Object handleReuqest(RpcRequest rpcRequest) throws Throwable {
		String requestId = rpcRequest.getRequestId();
		String interfaceName = rpcRequest.getInterfaceName();
		// 根据接口名拿到其实现类对象
		Object serivceBean = serviceBeanMap.get(interfaceName);
		// 拿到要调用的方法名、参数类型、参数值
		String methodName = rpcRequest.getMethodName();
		Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
		Object[] parameters = rpcRequest.getParameters();

		// 拿到接口类对象
		Class<?> clazz = Class.forName(interfaceName);
		// 拿到实现类对象的指定方法
		Method method = clazz.getMethod(methodName, parameterTypes);
		// 通过反射调用方法
		logger.info("requestId:{},准备通过反射调用方法:{}", requestId, interfaceName + "." + methodName);
		Object result = method.invoke(serivceBean, parameters);
		logger.info("requestId:{},通过反射调用方法完毕,结果:", requestId, result);
		// 返回结果
		return result;
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		ctx.close();
	}
}
