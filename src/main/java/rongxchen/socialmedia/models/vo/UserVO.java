package rongxchen.socialmedia.models.vo;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

/**
 * @author CHEN Rongxin
 */
@Data
public class UserVO {

	@Size(max = 100, min = 1)
	private String username;

	@Email
	private String email;

	private String appId;

	@Size(max = 500)
	private String description;

	private String sex;

	private String avatar;

	private String birthday;

	private String createTime;

	private String updateTime;

	@Data
	public static class SimpleUserVO {
		@Size(max = 100, min = 1)
		private String username;

		private String appId;

		private String avatar;
	}

}
