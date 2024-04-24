package rongxchen.socialmedia.service;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import rongxchen.socialmedia.models.dto.CommentDTO;
import rongxchen.socialmedia.models.entity.CollectItem;
import rongxchen.socialmedia.models.entity.Comment;
import rongxchen.socialmedia.models.entity.Post;
import rongxchen.socialmedia.models.entity.User;
import rongxchen.socialmedia.models.vo.CommentVO;
import rongxchen.socialmedia.repository.CollectItemRepository;
import rongxchen.socialmedia.repository.CommentRepository;
import rongxchen.socialmedia.repository.PostRepository;
import rongxchen.socialmedia.repository.UserRepository;
import rongxchen.socialmedia.service.azure.AzureBlobService;
import rongxchen.socialmedia.service.common.MyMongoService;
import rongxchen.socialmedia.utils.DateUtil;
import rongxchen.socialmedia.utils.UUIDGenerator;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author CHEN Rongxin
 */
@Service
public class CommentService {

	@Resource
	private CommentRepository commentRepository;

	@Resource
	private UserRepository userRepository;

	@Resource
	private PostRepository postRepository;

	@Resource
	private CollectItemRepository collectItemRepository;

	@Resource
	private MyMongoService myMongoService;

	@Resource
	private AzureBlobService azureBlobService;

	private final String BLOB_URL_PREFIX = "https://mysocialmediastorage.blob.core.windows.net/media";

	public CommentVO addComment(CommentDTO commentDTO, String userId) {
		// convert and store
		Comment comment = new Comment();
		String commentId = UUIDGenerator.generate("comment");
		while (commentRepository.getByCommentId(commentId) != null) {
			commentId = UUIDGenerator.generate("comment");
		}
		comment.setCommentId(commentId);
		comment.setContent(commentDTO.getContent());
		comment.setPostId(commentDTO.getPostId());
		comment.setParentId(commentDTO.getParentId());
		comment.setReplyCommentId(commentDTO.getReplyCommentId());
		comment.setReplyCommentUserId(commentDTO.getReplyCommentUserId());
		comment.setLikeCount(0);
		comment.setCommentCount(0);
		comment.setAuthorId(userId);
		comment.setCreateTime(LocalDateTime.now());
		// update post comment count
		Post post = postRepository.getByPostId(commentDTO.getPostId());
		if (post == null) {
			throw new IllegalArgumentException("no such post");
		}
		// find user info
		User user = userRepository.getByAppId(userId);
		User replyToUser = userRepository.getByAppId(commentDTO.getReplyCommentUserId());
		if (user == null || replyToUser == null) {
			throw new RuntimeException("no such user");
		}
		post.setCommentCount(post.getCommentCount()+1);
		postRepository.save(post);
		// add to comment's comment count if applicable
		if (!commentDTO.getPostId().equals(commentDTO.getParentId())) {
			Comment parentComment = commentRepository.getByCommentId(commentDTO.getParentId());
			if (parentComment != null) {
				parentComment.setCommentCount(parentComment.getCommentCount()+1);
				commentRepository.save(parentComment);
			}
		}
		commentRepository.save(comment);
		// convert and return
		CommentVO commentVO = new CommentVO();
		commentVO.setCommentId(comment.getCommentId());
		commentVO.setContent(comment.getContent());
		commentVO.setImage(comment.getImage());
		commentVO.setPostId(comment.getPostId());
		commentVO.setParentId(comment.getParentId());
		commentVO.setReplyCommentId(comment.getReplyCommentId());
		commentVO.setReplyCommentUsername(replyToUser.getUsername());
		commentVO.setLikeCount(comment.getLikeCount());
		commentVO.setCommentCount(comment.getCommentCount());
		commentVO.setAuthorId(comment.getAuthorId());
		commentVO.setCreateTime(DateUtil.convertToDisplayTime(comment.getCreateTime()));
		// set user info
		commentVO.setAuthorName(user.getUsername());
		commentVO.setAuthorAvatar(user.getAvatar());
		return commentVO;
	}

	public Map<String, Object> expandComments(String postId,
											  String parentId,
											  String sortField,
											  Integer order,
											  Integer offset) {
		int defaultCommentSize = 8;
		List<CommentVO> commentList = postId.equals(parentId) ? myMongoService
				.lookup("users", "appId", "authorId", "userInfo")
				.unwind("userInfo")
				.match(Criteria.where("postId").is(postId).and("parentId").is(parentId))
				.project("commentId", "content", "image", "postId", "parentId", "replyCommentId",
						"likeCount", "authorId", "createTime", "userInfo.username as authorName",
						"userInfo.avatar as authorAvatar", "commentCount")
				.skip(offset)
				.limit(defaultCommentSize)
				.sort(sortField, order)
				.fetchResult("comments", CommentVO.class) : myMongoService
						.lookup("users", "appId", "authorId", "userInfo")
						.lookup("users", "appId", "replyCommentUserId", "replyCommentUser")
						.unwind("userInfo")
						.unwind("replyCommentUser")
						.match(Criteria.where("postId").is(postId).and("parentId").is(parentId))
						.project("commentId", "content", "image", "postId", "parentId", "replyCommentId",
								"likeCount", "authorId", "createTime", "userInfo.username as authorName",
								"userInfo.avatar as authorAvatar", "commentCount",
								"replyCommentUser.username as replyCommentUsername", "replyCommentUserId")
						.skip(offset)
						.limit(defaultCommentSize)
						.sort(sortField, order)
						.fetchResult("comments", CommentVO.class);
		for (CommentVO commentVO : commentList) {
			commentVO.setCreateTime(DateUtil.convertToDisplayTime(commentVO.getCreateTime()));
		}
		Map<String, Object> map = new HashMap<>();
		map.put("list", commentList);
		map.put("count", commentList.size());
		return map;
	}

	public Map<String, Object> getComments(String postId, Integer offset) {
		return expandComments(postId, postId, "likeCount", -1, offset);
	}

	public Map<String, Object> getSubComments(String postId, String parentId, Integer offset) {
		return expandComments(postId, parentId, "createTime", 1, offset);
	}

	public boolean likeComment(String commentId, String action, String userId) {
		Comment comment = commentRepository.getByCommentId(commentId);
		if (comment == null) {
			throw new IllegalArgumentException("comment not exist");
		}
		if ("collect".equals(action)) {
			comment.setLikeCount(comment.getLikeCount()+1);
			commentRepository.save(comment);
			CollectItem item = new CollectItem();
			item.setItemId(commentId);
			item.setUserId(userId);
			item.setItemType("comment-likes");
			item.setItemMeta("comment");
			collectItemRepository.save(item);
		} else if ("cancel".equals(action)) {
			comment.setLikeCount(comment.getLikeCount()-1);
			commentRepository.save(comment);
			CollectItem item = collectItemRepository.getByItemId(commentId, "comment-likes", userId);
			collectItemRepository.delete(item);
		} else {
			return false;
		}
		return true;
	}

	public boolean deleteComment(String commentId, String userId) {
		Comment comment = commentRepository.getByCommentId(commentId);
		if (comment == null || !comment.getAuthorId().equals(userId)) {
			throw new RuntimeException("failed to delete comment");
		}
		// delete image if any
		if (comment.getImage() != null && !comment.getImage().isEmpty()) {
			String blobName = comment.getImage().replace(BLOB_URL_PREFIX + "/", "");
			azureBlobService.removeFile("media", blobName);
		}
		// update post comment count (and parent comment count if applicable)
		Post post = postRepository.getByPostId(comment.getPostId());
		if (post == null) {
			throw new RuntimeException("no such post");
		}
		post.setCommentCount(post.getCommentCount()-1);
		postRepository.save(post);
		if (!comment.getParentId().equals(comment.getPostId())) {
			Comment parentComment = commentRepository.getByCommentId(comment.getParentId());
			if (parentComment == null) {
				throw new RuntimeException("no such parent comment");
			}
			parentComment.setCommentCount(parentComment.getCommentCount()-1);
			commentRepository.save(parentComment);
		}
		// delete comment
		commentRepository.delete(comment);
		return true;
	}

}
