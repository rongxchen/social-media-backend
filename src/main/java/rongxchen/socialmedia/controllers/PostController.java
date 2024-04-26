package rongxchen.socialmedia.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rongxchen.socialmedia.common.annotations.LoginToken;
import rongxchen.socialmedia.models.Result;
import rongxchen.socialmedia.models.vo.PostVO;
import rongxchen.socialmedia.service.PostService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

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
	public Result<List<PostVO>> getPosts(@RequestParam("offset") Integer offset,
										 @RequestParam(value = "userId", required = false) String userId) {
		List<PostVO> posts = userId == null ? postService.getPostByPage(offset)
				: postService.getPostOfUser(userId, offset);
		return Result.success(posts);
	}

	@GetMapping("/collected")
	public Result<List<PostVO>> getCollectedPosts(@RequestParam("offset") Integer offset,
												  @RequestParam("itemType") String itemType,
												  @RequestParam("userId") String userId) {
		List<PostVO> collectedPosts = postService.getCollectedPostOfUser(userId, itemType, offset);
		return Result.success(collectedPosts);
	}

	@GetMapping("/{postId}")
	public Result<PostVO> getPost(@PathVariable("postId") String postId) {
		PostVO postVO = postService.getPostByPostId(postId);
		return Result.success(postVO);
	}

	@PostMapping("/like-post")
	public Result<Boolean> likePost(@RequestParam("postId") String postId,
									@RequestParam("action") String action,
									@RequestAttribute String appId) {
		boolean success = postService.collectPost(postId, action, "likes", appId);
		return Result.success(success);
	}

	@PostMapping("/favorite-post")
	public Result<Boolean> favoritePost(@RequestParam("postId") String postId,
										@RequestParam("action") String action,
										@RequestAttribute String appId) {
		boolean success = postService.collectPost(postId, action, "favorites", appId);
		return Result.success(success);
	}

	@GetMapping("/record")
	public Result<Map<String, List<String>>> getLikesRecord(@RequestAttribute String appId) {
		Map<String, List<String>> record = postService.getCollectionRecord(appId);
		return Result.success(record);
	}

	@DeleteMapping
	public Result<Boolean> deletePost(@RequestParam("postId") String postId,
									  @RequestAttribute String appId) {
		postService.deletePost(postId, appId);
		return Result.success(true);
	}

}
