package rongxchen.socialmedia;

import java.util.List;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import rongxchen.socialmedia.models.vo.notifications.LikesNotificationVO;
import rongxchen.socialmedia.service.NotificationService;

/**
 * @author CHEN Rongxin
 */
@SpringBootTest
public class GeneralTest {

	@Resource
	NotificationService service;

	@Test
	void test() {
		long skip = 0;
		long limit = 20;
		List<LikesNotificationVO> list = service.getLikesNotificationList("64af2195f02f4e1496c6e58969824ddb", skip, limit);
		list.forEach(System.out::println);
	}

}
