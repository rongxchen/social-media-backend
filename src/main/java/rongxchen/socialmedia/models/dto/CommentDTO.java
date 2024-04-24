package rongxchen.socialmedia.models.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

/**
 * @author CHEN Rongxin
 */
@Data
public class CommentDTO {

	private String content;

	private MultipartFile image;

	@NotNull
	private String postId;

	@NotNull
	private String parentId;

	private String replyCommentId;

	private String replyCommentUserId;

}
