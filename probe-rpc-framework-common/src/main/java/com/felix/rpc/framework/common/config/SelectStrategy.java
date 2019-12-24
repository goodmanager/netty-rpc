package com.felix.rpc.framework.common.config;

public enum SelectStrategy {

	RANDOM(1, "Random"), ROUNDROBIN(2, "RoundRobin");

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
