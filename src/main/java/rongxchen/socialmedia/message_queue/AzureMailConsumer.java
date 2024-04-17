package rongxchen.socialmedia.message_queue;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import rongxchen.socialmedia.models.mq.MQBody;
import rongxchen.socialmedia.service.azure.AzureMailService;
import rongxchen.socialmedia.utils.ObjectUtil;

import javax.annotation.Resource;

/**
 * @author CHEN Rongxin
 */
@Component
@RocketMQMessageListener(
		topic = "azure-mail",
		consumerGroup = "${rocketmq.consumer.group}"
)
@Slf4j
public class AzureMailConsumer implements RocketMQListener<String> {

	@Resource
	private AzureMailService azureMailService;

	@Resource
	private ObjectUtil objectUtil;

	@Override
	public void onMessage(String message) {
		MQBody mqBody = objectUtil.read(message, MQBody.class);
		log.info(mqBody.toString());
		switch (mqBody.getMessageType()) {
			case "mail_verification_code": {
				String email = mqBody.get("email");
				String code = mqBody.get("code");
				azureMailService.sendVerificationCode(email, code);
				break;
			}
			case "mail_reset_password": {
				String appId = mqBody.get("appId");
				String username = mqBody.get("username");
				String email = mqBody.get("email");
				azureMailService.sendResetPassword(appId, username, email);
				break;
			}
		}
	}

}
