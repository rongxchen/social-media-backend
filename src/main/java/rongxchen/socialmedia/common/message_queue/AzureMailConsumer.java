package rongxchen.socialmedia.common.message_queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import rongxchen.socialmedia.exceptions.HttpException;
import rongxchen.socialmedia.models.mq.MessageMeta;
import rongxchen.socialmedia.service.azure.AzureMailService;

import javax.annotation.Resource;

/**
 * @author CHEN Rongxin
 */
@Component
@RocketMQMessageListener(
		topic = "azure-mail",
		consumerGroup = "${rocketmq.consumer.group}")
public class AzureMailConsumer implements RocketMQListener<String> {

	@Resource
	private AzureMailService azureMailService;

	@Resource
	private ObjectMapper objectMapper;

	@Override
	public void onMessage(String message) {
		MessageMeta messageMeta;
		try {
			messageMeta = objectMapper.readValue(message, MessageMeta.class);
		} catch (JsonProcessingException e) {
			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to parse message");
		}
		switch (messageMeta.getMessageType()) {
			case MAIL_VERIFICATION_CODE -> {
				String email = messageMeta.get("email");
				String code = messageMeta.get("code");
				azureMailService.sendVerificationCode(email, code);
			}
			case MAIL_RESET_PASSWORD -> {
				String appId = messageMeta.get("appId");
				String username = messageMeta.get("username");
				String email = messageMeta.get("email");
				azureMailService.sendResetPassword(appId, username, email);
			}
		}
	}

}
