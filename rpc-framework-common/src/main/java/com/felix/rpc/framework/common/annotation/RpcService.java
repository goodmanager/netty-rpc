package com.felix.rpc.framework.common.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * 服务提供方发布服务的注解
 * 
 * @author phfelix
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
@Component
public @interface RpcService {
	// 服务发现用的唯一标识，用于服务自动寻址
	String value() default "";

	Class<?> targetInterface(); // 防止实现类实现了多个接口

	String debugAddress() default "";
}
