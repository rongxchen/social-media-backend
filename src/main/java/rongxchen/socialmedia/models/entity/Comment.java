package rongxchen.socialmedia.models.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * @author CHEN Rongxin
 */
@Data
@Document(collection = "comments")
public class Comment {

	@Id
	private String id;

	private String commentId;

	private String content;

	private String image;

	private String postId;

	private String parentId;

	private String replyCommentId;

	private Integer likeCount;

	private Integer commentCount;

	private String authorId;

	private LocalDateTime createTime;

}
