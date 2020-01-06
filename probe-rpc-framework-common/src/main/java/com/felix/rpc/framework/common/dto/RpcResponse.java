package com.felix.rpc.framework.common.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RpcResponse {

	private String requestId;

	private Throwable error;

	private Object result;

}
