package com.felix.rpc.framework.common.exception;

import lombok.ToString;

/**
 * Rpc Framework 异常
 * 
 * @author coral
 *
 */
@ToString
public class RpcFrameworkException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3675409442107808217L;

	private String message;

	public RpcFrameworkException(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
