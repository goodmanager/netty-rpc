package com.felix.rpc.framework.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.netty-server")
public class NettyServerConfig {

	private String hostName;

	private int port;

	private SelectStrategy selectStrategy;

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
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
