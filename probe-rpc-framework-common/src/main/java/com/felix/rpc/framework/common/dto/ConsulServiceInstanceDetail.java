package com.felix.rpc.framework.common.dto;

import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ConsulServiceInstanceDetail {

	private String id;

	private String hostName;

	private int listenPort;

	private String interfaceName;

	private List<String> tags;

}
