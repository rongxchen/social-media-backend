package rongxchen.socialmedia.models.mq;

import lombok.Data;
import rongxchen.socialmedia.common.enums.MessageType;

import java.io.Serializable;
import java.util.Map;

/**
 * @author CHEN Rongxin
 */
@Data
public class MessageMeta implements Serializable {

	private MessageType messageType;

	private Map<String, Object> data;

	public String get(String key) {
		return data.getOrDefault(key, "").toString();
	}

}
