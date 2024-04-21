package rongxchen.socialmedia;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import rongxchen.socialmedia.message_queue.RocketMQProducer;
import rongxchen.socialmedia.models.entity.CollectItem;
import rongxchen.socialmedia.models.mq.MQBody;
import rongxchen.socialmedia.repository.CollectItemRepository;
import rongxchen.socialmedia.utils.ObjectUtil;

import javax.annotation.Resource;

/**
 * @author CHEN Rongxin
 */
@SpringBootTest
public class GeneralTest {

	@Resource
	CollectItemRepository collectItemRepository;

	@Test
	void test() {
		CollectItem item = collectItemRepository.getByItemId(
				"post-9c089c5c-1672-416d-8a5e-cdb466097d53",
				"likes",
				"259a002f790042928e918a2e60a013ee");
		System.out.println(item);
		collectItemRepository.delete(item);
	}

}
