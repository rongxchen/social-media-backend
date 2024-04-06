package rongxchen.socialmedia.models.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Base64;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author CHEN Rongxin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageMeta implements Serializable {

	private String messageType;

	private Map<String, Object> data;

	public MessageMeta(String messageType) {
		this.messageType = messageType;
		data = new HashMap<>();
	}

	public <T> T get(String key) {
		return (T) data.getOrDefault(key, null);
	}

	public int getInt(String key) {
		return (int) data.getOrDefault(key, "");
	}

	public String getString(String key) {
		return data.getOrDefault(key, "").toString();
	}

	public byte[] getBytes(String key) {
		return Base64.decodeBase64(data.getOrDefault(key, "").toString());
	}

	public void add(String key, Object value) {
		data.put(key, value);
	}

	public void addInt(String key, int value) {
		data.put(key, value);
	}

	public void addString(String key, String value) {
		data.put(key, value);
	}

	public void addBytes(String key, byte[] value) {
		data.put(key, Base64.encodeBase64String(value));
	}

}
