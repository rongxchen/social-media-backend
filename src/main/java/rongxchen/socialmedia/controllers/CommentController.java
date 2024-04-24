package rongxchen.socialmedia.controllers;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rongxchen.socialmedia.common.annotations.LoginToken;
import rongxchen.socialmedia.models.Result;
import rongxchen.socialmedia.models.dto.CommentDTO;
import rongxchen.socialmedia.models.vo.CommentVO;
import rongxchen.socialmedia.service.CommentService;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author CHEN Rongxin
 */
@RestController
@LoginToken
@RequestMapping("/api/comments")
public class CommentController {

	@Resource
	private CommentService commentService;

	@PostMapping
	public Result<CommentVO> addComment(@RequestBody @Validated CommentDTO commentDTO,
										@RequestAttribute String appId) {
		CommentVO commentVO = commentService.addComment(commentDTO, appId);
		return Result.success(commentVO);
	}

	@GetMapping
	public Result<Map<String, Object>> getComments(@RequestParam("postId") String postId,
												   @RequestParam("offset") Integer offset) {
		Map<String, Object> comments = commentService.getComments(postId, offset);
		return Result.success(comments);
	}

	@GetMapping("/sub-comments")
	public Result<Map<String, Object>> getSubComments(@RequestParam("postId") String postId,
													  @RequestParam("parentId") String parentId,
													  @RequestParam("offset") Integer offset) {
		Map<String, Object> comments = commentService.getSubComments(postId, parentId, offset);
		return Result.success(comments);
	}

	@PostMapping("/like-comment")
	public Result<Boolean> likeComment(@RequestParam("commentId") String commentId,
									   @RequestParam("action") String action,
									   @RequestAttribute String appId) {
		boolean success = commentService.likeComment(commentId, action, appId);
		return Result.success(success);
	}

	@DeleteMapping
	public Result<Boolean> deleteComment(@RequestParam("commentId") String commentId,
										 @RequestAttribute String appId) {
		boolean deleted = commentService.deleteComment(commentId, appId);
		return Result.success(deleted);
	}

}
