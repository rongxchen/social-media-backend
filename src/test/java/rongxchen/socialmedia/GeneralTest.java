package rongxchen.socialmedia;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import rongxchen.socialmedia.message_queue.RocketMQProducer;
import rongxchen.socialmedia.models.mq.MQBody;
import rongxchen.socialmedia.utils.ObjectUtil;

import javax.annotation.Resource;

/**
 * @author CHEN Rongxin
 */
@SpringBootTest
public class GeneralTest {

	@Resource
	RocketMQProducer rocketMQProducer;

	@Test
	void test() {

	}

}
