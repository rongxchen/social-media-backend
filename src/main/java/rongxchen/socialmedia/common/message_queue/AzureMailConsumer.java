package rongxchen.socialmedia.common.message_queue;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import rongxchen.socialmedia.models.mq.MessageMeta;
import rongxchen.socialmedia.service.azure.AzureMailService;
import rongxchen.socialmedia.utils.ObjectUtil;

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
	private ObjectUtil objectUtil;

	@Override
	public void onMessage(String message) {
		MessageMeta messageMeta = objectUtil.readObject(message, MessageMeta.class);
		System.out.println(messageMeta);
		switch (messageMeta.getMessageType()) {
			case "mail_verification_code": {
				String email = messageMeta.getString("email");
				String code = messageMeta.getString("code");
				azureMailService.sendVerificationCode(email, code);
				break;
			}
			case "mail_reset_password": {
				String appId = messageMeta.getString("appId");
				String username = messageMeta.getString("username");
				String email = messageMeta.getString("email");
				azureMailService.sendResetPassword(appId, username, email);
				break;
			}
		}
	}

}
