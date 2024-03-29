package rongxchen.socialmedia.common.message_queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import rongxchen.socialmedia.exceptions.HttpException;

import javax.annotation.Resource;

/**
 * @author CHEN Rongxin
 */
@Component
public class RocketMQProducer {

	@Resource
	private RocketMQTemplate rocketMQTemplate;

	@Resource
	private ObjectMapper objectMapper;

	public <T> void sendMessage(String topic, T message) {
		try {
			String messageString = objectMapper.writeValueAsString(message);
			rocketMQTemplate.convertAndSend(topic, messageString);
		} catch (JsonProcessingException e) {
			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to write message");
		}
	}

}
