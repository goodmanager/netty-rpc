package com.felix.rpc.framework.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * 服务提供方发布服务的注解
 * 
 * @author phfelix
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface RpcService {
	// 服务发现用的唯一标识，用于服务自动寻址
	String value() default "";

	Class<?> targetInterface(); // 防止实现类实现了多个接口

	String debugAddress() default "";
}
