package com.felix.rpc.framework.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.netty-server")
public class NettyServerConfig {

	private String hostName;

	private int port;

	private SelectServiceStrategy selectServiceStrategy;

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

	public SelectServiceStrategy getSelectServiceStrategy() {
		return selectServiceStrategy;
	}

	public void setSelectServiceStrategy(SelectServiceStrategy selectServiceStrategy) {
		this.selectServiceStrategy = selectServiceStrategy;
	}

}
