package rongxchen.socialmedia.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import rongxchen.socialmedia.exceptions.HttpException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author CHEN Rongxin
 */
@Component
public class ObjectUtil {

	@Resource
	private ObjectMapper objectMapper;

	public <T> T read(String objectString, Class<T> clazz) {
		try {
			return objectMapper.readValue(objectString, clazz);
		} catch (JsonProcessingException e) {
			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to parse object");
		}
	}

	public <T> List<T> readList(String objectListString, Class<T> clazz) {
		try {
			CollectionType collectionType = objectMapper.getTypeFactory()
					.constructCollectionType(ArrayList.class, clazz);
			return objectMapper.readValue(objectListString, collectionType);
		} catch (JsonProcessingException e) {
			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to parse object");
		}
	}

	public <T> String write(T object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to write object");
		}
	}

	public <T> String writeList(List<T> object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to write object");
		}
	}

}
