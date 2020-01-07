package com.felix.rpc.framework.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * netty server 配置
 * 
 * @author felix
 *
 */
@Component
@ConfigurationProperties(prefix = "spring.netty-server")
public class NettyServerConfig {

	private String ipAddr;

	private int port;

	private SelectStrategy selectStrategy;

	public String getIpAddr() {
		return ipAddr;
	}

	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public SelectStrategy getSelectStrategy() {
		return selectStrategy;
	}

	public void setSelectStrategy(SelectStrategy selectStrategy) {
		this.selectStrategy = selectStrategy;
	}

}
