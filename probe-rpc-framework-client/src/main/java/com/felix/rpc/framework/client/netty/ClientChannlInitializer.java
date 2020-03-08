package com.felix.rpc.framework.client.netty;

import com.felix.rpc.framework.common.codec.RpcDecoder;
import com.felix.rpc.framework.common.codec.RpcEncoder;
import com.felix.rpc.framework.common.dto.RpcRequest;
import com.felix.rpc.framework.common.dto.RpcResponse;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class ClientChannlInitializer extends ChannelInitializer<SocketChannel> {

	private String host;

	private int port;

	public ClientChannlInitializer(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	protected void initChannel(SocketChannel sch) throws Exception {
		ChannelPipeline cp = sch.pipeline();
		// 添加编码器，Rpc服务端需要解码的是RpcRequest对象，因为需要接收客户端发送过来的请求
		cp.addLast(new RpcEncoder(RpcRequest.class));
		// 添加解码器
		cp.addLast(new RpcDecoder(RpcResponse.class));
		// 添加业务处理handler
		cp.addLast(new RpcClient(host, port));
	}
}
