package rongxchen.socialmedia;

import java.util.List;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import rongxchen.socialmedia.models.vo.PostVO;
import rongxchen.socialmedia.models.vo.notifications.CommentsNotificationVO;
import rongxchen.socialmedia.service.NotificationService;
import rongxchen.socialmedia.service.PostService;

/**
 * @author CHEN Rongxin
 */
@SpringBootTest
public class GeneralTest {

	@Resource
	NotificationService service;

	@Test
	void test() {
		List<CommentsNotificationVO> list = service.getCommentsNotificationList("5538e1372bb8454798147bb7293ac529", 0, 10);
		list.forEach(System.out::println);
	}

}
