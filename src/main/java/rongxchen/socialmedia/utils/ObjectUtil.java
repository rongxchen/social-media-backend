package rongxchen.socialmedia.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import rongxchen.socialmedia.exceptions.HttpException;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author CHEN Rongxin
 */
@Component
public class ObjectUtil {

	@Resource
	private ObjectMapper objectMapper;

	public <T> T readObject(String objectString, Class<T> clazz) {
		T obj;
		try {
			obj = objectMapper.readValue(objectString, clazz);
		} catch (JsonProcessingException e) {
			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to parse object");
		}
		return obj;
	}

	public <T> List<T> readObjectList(String objectListString, Class<T> clazz) {
		List<T> obj;
		try {
			obj = objectMapper.readValue(objectListString, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
		} catch (JsonProcessingException e) {
			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to parse object");
		}
		return obj;
	}

	public <T> String writeObjectAsString(T object) {
		String objectString;
		try {
			objectString = objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to write object");
		}
		return objectString;
	}

	public <T> String writeObjectListAsString(List<T> object) {
		String objectString;
		try {
			objectString = objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to write object");
		}
		return objectString;
	}

	public  <T> byte[] writeObjectAsBytes(T object) {
		byte[] objectString;
		try {
			objectString = objectMapper.writeValueAsBytes(object);
		} catch (JsonProcessingException e) {
			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to write object");
		}
		return objectString;
	}

}
