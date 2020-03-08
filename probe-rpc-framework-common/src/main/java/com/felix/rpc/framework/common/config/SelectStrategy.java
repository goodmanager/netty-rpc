package com.felix.rpc.framework.common.config;

/**
 * 负载均衡策略
 * 
 * @author felix
 *
 */
public enum SelectStrategy {

	RANDOM(1, "Random"), // 随机
	ROUNDROBIN(2, "RoundRobin"), // 轮训
	BESTAVAILABLE(3, "BestAvailable"), // 并发数低
	WEIGHTEDRESPONSETIME(4, "WeightedResponseTime");// 加权

	private int index;
	private String description;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	SelectStrategy(int index, String description) {
		this.index = index;
		this.description = description;
	}

}
