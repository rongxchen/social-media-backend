package rongxchen.socialmedia;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import rongxchen.socialmedia.models.dto.UserDTO;
import rongxchen.socialmedia.models.entity.User;
import rongxchen.socialmedia.models.vo.UserVO;
import rongxchen.socialmedia.repository.UserRepository;
import rongxchen.socialmedia.service.UserService;
import rongxchen.socialmedia.utils.EncryptionUtil;
import rongxchen.socialmedia.utils.JwtUtil;

import javax.annotation.Resource;

/**
 * @author CHEN Rongxin
 */
@SpringBootTest
public class GeneralTest {

	@Resource
	UserService userService;

	@Test
	void test() {
		userService.sendVerificationCode("chenrongxin20020102@gmail.com");
	}

}
