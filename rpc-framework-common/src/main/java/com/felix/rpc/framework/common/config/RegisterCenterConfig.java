package com.felix.rpc.framework.common.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.register-center")
public class RegisterCenterConfig {

	private List<String> hosts;

	private String basePath;

	private RegisterCenterType registerCenterType;

	private SelectStrategy selectStrategy;

	public List<String> getHosts() {
		return hosts;
	}

	public void setHosts(List<String> hosts) {
		this.hosts = hosts;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public RegisterCenterType getRegisterCenterType() {
		return registerCenterType;
	}

	public void setRegisterCenterType(RegisterCenterType registerCenterType) {
		this.registerCenterType = registerCenterType;
	}

	public SelectStrategy getSelectStrategy() {
		return selectStrategy;
	}

	public void setSelectStrategy(SelectStrategy selectStrategy) {
		this.selectStrategy = selectStrategy;
	}

}
