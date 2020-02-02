package com.felix.rpc.framework.common.exception;

/**
 * Rpc Framework 异常
 * 
 * @author coral
 *
 */
public class RpcFrameworkException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3675409442107808217L;

	private int errorCode;

	private String message;

	public RpcFrameworkException(String message) {
		this.message = message;
	}

	public RpcFrameworkException(int errorCode, String message) {
		this.errorCode = errorCode;
		this.message = message;
	}

	@Override
	public String toString() {
		return "RpcFrameworkException[errorCode=" + errorCode + ",message=" + message + "]";
	}

}
