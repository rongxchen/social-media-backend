package rongxchen.socialmedia;

import java.util.List;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import rongxchen.socialmedia.models.vo.PostVO;
import rongxchen.socialmedia.service.PostService;

/**
 * @author CHEN Rongxin
 */
@SpringBootTest
public class GeneralTest {

	@Resource
	PostService postService;

	@Test
	void test() {
		List<PostVO> items = postService.getCollectedPostOfUser("259a002f790042928e918a2e60a013ee", "likes", 0);
		for (PostVO item : items) {
			System.out.println(item);
		}
	}

}
