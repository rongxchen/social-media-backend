package rongxchen.socialmedia.models.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author CHEN Rongxin
 */
@Data
@Document(collection = "posts")
public class Post {

	@Id
	private String id;

	@NotNull
	private String postId;

	@NotNull
	@Min(1)
	private String title;

	@NotNull
	@Min(1)
	private String content;

	private List<String> imageList;

	private List<String> tagList;

	@NotNull
	private String authorId;

	private Integer likeCount;

	private Integer favoriteCount;

	private Integer commentCount;

	@NotNull
	private LocalDateTime createTime;

	private LocalDateTime lastModifiedTime;

}
