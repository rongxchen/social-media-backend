package rongxchen.socialmedia.common.message_queue;

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;
import rongxchen.socialmedia.models.mq.MessageMeta;
import rongxchen.socialmedia.utils.ObjectUtil;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author CHEN Rongxin
 */
@Component
public class RocketMQProducer {

	@Resource
	private RocketMQTemplate rocketMQTemplate;

	@Resource
	private ObjectUtil objectUtil;

	public void sendMessage(String topic, MessageMeta messageMeta) {
		rocketMQTemplate.convertAndSend(topic, objectUtil.writeObjectAsString(messageMeta));
	}

	public void sendMessage(String topic, List<MessageMeta> objectList) {
		rocketMQTemplate.convertAndSend(topic, objectUtil.writeObjectListAsString(objectList));
	}

}

@Component
class RocketMQSendCallback implements SendCallback {

	@Override
	public void onSuccess(SendResult sendResult) {
		System.out.println("send success: " + sendResult);
	}

	@Override
	public void onException(Throwable throwable) {
		System.out.println("exception here: " + throwable);
	}

}
