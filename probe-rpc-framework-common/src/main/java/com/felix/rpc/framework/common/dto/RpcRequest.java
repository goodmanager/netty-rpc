package com.felix.rpc.framework.common.dto;

import lombok.Data;
import lombok.ToString;

/**
 * client向server端发送数据的传输载体,将要传输的对象封装到RpcRequest对象中
 * 
 * @author phfelix
 *
 */
@Data
@ToString
public class RpcRequest {

	private String requestId;
	// 接口名称
	private String interfaceName;
	// 调用的方法名称
	private String methodName;
	// 方法的参数类型
	private Class<?>[] parameterTypes;
	// 方法的参数值
	private Object[] parameters;

	public String getRequestId() {
		return requestId;
	}

}
