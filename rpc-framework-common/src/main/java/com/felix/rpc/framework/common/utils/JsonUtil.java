package com.felix.rpc.framework.common.utils;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {

	private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

	public static String objectToString(Object object) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			logger.error("对象转json字符串出错", e);
		}
		return null;
	}

	public static <T> T parseStringToObject(String jsonString, Class<T> cls) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(jsonString, cls);
		} catch (JsonParseException e) {
			logger.error("json字符串转对象出错", e);
		} catch (JsonMappingException e) {
			logger.error("json字符串转对象出错", e);
		} catch (IOException e) {
			logger.error("json字符串转对象出错", e);
		}
		return null;
	}

}
