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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * @author CHEN Rongxin
 */
@SpringBootTest
public class GeneralTest {

	@Resource
	CollectItemRepository collectItemRepository;

	@Test
	void test() {
		LocalDateTime parse = LocalDateTime.parse("2024-04-03T00:00:00");
		System.out.println(parse);
	}

}
