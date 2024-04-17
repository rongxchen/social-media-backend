package rongxchen.socialmedia.message_queue;

import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;
import rongxchen.socialmedia.models.mq.MQBody;
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

	public void sendMessage(String topic, MQBody messageMeta) {
		rocketMQTemplate.convertAndSend(topic, objectUtil.write(messageMeta));
	}

	public SendResult syncSendMessage(String topic, MQBody messageMeta) {
		return rocketMQTemplate.syncSend(topic, objectUtil.write(messageMeta));
	}

	public void sendMessage(String topic, List<MQBody> objectList) {
		rocketMQTemplate.convertAndSend(topic, objectUtil.writeList(objectList));
	}

	public SendResult syncSendMessage(String topic, List<MQBody> messageMeta) {
		return rocketMQTemplate.syncSend(topic, objectUtil.writeList(messageMeta));
	}

}
