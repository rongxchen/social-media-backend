package rongxchen.socialmedia.models.vo;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * @author CHEN Rongxin
 */
@Data
public class UserVO {

	@Size(max = 50, min = 8)
	private String username;

	@Email
	private String email;

	private String appId;

	@Size(max = 500)
	private String description;

	private String sex;

	private String avatar;

	private LocalDate birthday;

	private LocalDate createTime;

	private LocalDate updateTime;

}
