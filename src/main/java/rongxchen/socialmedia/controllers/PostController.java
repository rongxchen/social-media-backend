package rongxchen.socialmedia.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rongxchen.socialmedia.common.annotations.LoginToken;
import rongxchen.socialmedia.message_queue.RocketMQProducer;
import rongxchen.socialmedia.models.Result;
import rongxchen.socialmedia.models.mq.MQBody;
import rongxchen.socialmedia.models.vo.PostVO;
import rongxchen.socialmedia.service.PostService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author CHEN Rongxin
 */
@RestController
@RequestMapping("/api/posts")
@LoginToken
public class PostController {

	@Resource
	private PostService postService;

	@PostMapping
	public Result<String> uploadPost(@RequestParam("files") MultipartFile[] files,
									 @RequestParam("post") String postJsonString,
									 @RequestAttribute String appId) {
		String postId = postService.publishPost(files, postJsonString, appId);
		return Result.success(postId);
	}

	@GetMapping
	public Result<List<PostVO>> getPosts(@RequestParam("page") long page) {
		List<PostVO> posts = postService.getPostByPage(page);
		return Result.success(posts);
	}

	@GetMapping("/{postId}")
	public Result<PostVO> getPost(@PathVariable("postId") String postId) {
		PostVO postVO = postService.getPostByPostId(postId);
		return Result.success(postVO);
	}

	@DeleteMapping
	public Result<Boolean> deletePost(@RequestParam("postId") String postId,
									  @RequestAttribute  String appId) {
		postService.deletePost(postId, appId);
		return Result.success(true);
	}

}
