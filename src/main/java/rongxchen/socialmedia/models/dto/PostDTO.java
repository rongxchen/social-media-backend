package rongxchen.socialmedia.models.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author CHEN Rongxin
 */
@Data
public class PostDTO {

	@NotNull
	@Min(1)
	private String title;

	@NotNull
	@Min(1)
	private String content;

	private List<String> tags;

}
