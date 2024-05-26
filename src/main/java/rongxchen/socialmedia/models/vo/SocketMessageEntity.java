package rongxchen.socialmedia.models.vo;

import lombok.Data;

/**
 * @author CHEN Rongxin
 */
@Data
public class SocketMessageEntity<T> {

	private String topic;

	private T data;

	public SocketMessageEntity(String topic, T data) {
		this.topic = topic;
		this.data = data;
	}

}
