package rongxchen.socialmedia.models.vo;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * @author CHEN Rongxin
 */
@Data
public class PostVO {

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

	private String authorName;

	private String authorAvatar;

	private Integer likeCount;

	private Integer favoriteCount;

	private Integer commentCount;

	private LocalDate createTime;

	private LocalDate lastModifiedTime;

}
