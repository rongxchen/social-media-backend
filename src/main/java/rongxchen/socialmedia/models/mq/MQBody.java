package rongxchen.socialmedia.models.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Base64;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * @author CHEN Rongxin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MQBody implements Serializable {

	private String messageType;

	private LinkedHashMap<String, Object> data = new LinkedHashMap<>();

	public MQBody(String messageType) {
		this.messageType = messageType;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String key) {
		return (T) data.getOrDefault(key, "");
	}

	public byte[] getBytes(String key) {
		return Base64.decodeBase64(data.getOrDefault(key, "").toString());
	}

	public <T> void add(String key, T value) {
		data.put(key, value);
	}

	public void addBytes(String key, byte[] value) {
		data.put(key, Base64.encodeBase64String(value));
	}

}
