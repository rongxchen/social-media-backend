package rongxchen.socialmedia.models.vo;

import lombok.Data;

/**
 * @author CHEN Rongxin
 */
@Data
public class CommentVO {

	private String commentId;

	private String content;

	private String image;

	private String postId;

	private String parentId;

	private String replyCommentId;

	private String replyCommentUserId;

	private String replyCommentUsername;

	private Integer likeCount;

	private Integer commentCount;

	private String authorId;

	private String authorName;

	private String authorAvatar;

	private String createTime;

}
