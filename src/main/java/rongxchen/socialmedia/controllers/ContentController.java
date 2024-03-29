package rongxchen.socialmedia.controllers;

import org.springframework.web.bind.annotation.*;
import rongxchen.socialmedia.common.annotations.LoginToken;
import rongxchen.socialmedia.common.message_queue.RocketMQProducer;
import rongxchen.socialmedia.models.Result;
import rongxchen.socialmedia.models.dto.UserDTO;

import javax.annotation.Resource;

/**
 * @author CHEN Rongxin
 */
@RestController
@RequestMapping("/api/contents")
public class ContentController {

	@Resource
	private RocketMQProducer rocketMQProducer;

	@PostMapping("/test-mq")
	@LoginToken(required = false)
	public Result<String> testMQ(@RequestBody UserDTO userDTO) {
		rocketMQProducer.sendMessage("test", userDTO.toString());
		return Result.ok();
	}

}
