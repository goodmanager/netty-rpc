package com.felix.rpc.framework.common.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ZkServiceInstanceDetail {

	private String id;

	private String listenAddress;

	private int listenPort;

	private String interfaceName;

	public ZkServiceInstanceDetail() {
	}

	public ZkServiceInstanceDetail(String id, String listenAddress, int listenPort, String interfaceName) {
		this.id = id;
		this.listenAddress = listenAddress;
		this.listenPort = listenPort;
		this.interfaceName = interfaceName;
	}

}
