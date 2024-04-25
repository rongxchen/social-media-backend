package rongxchen.socialmedia;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import rongxchen.socialmedia.message_queue.RocketMQProducer;
import rongxchen.socialmedia.models.entity.CollectItem;
import rongxchen.socialmedia.models.mq.MQBody;
import rongxchen.socialmedia.models.vo.UserVO;
import rongxchen.socialmedia.repository.CollectItemRepository;
import rongxchen.socialmedia.service.UserService;
import rongxchen.socialmedia.utils.ObjectUtil;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * @author CHEN Rongxin
 */
@SpringBootTest
public class GeneralTest {

	@Resource
	UserService userService;

	@Test
	void test() {
		List<UserVO.SimpleUserVO> followsList = userService.getFollowsList("259a002f790042928e918a2e60a013ee", 0);
		followsList.forEach(System.out::println);
	}

}
