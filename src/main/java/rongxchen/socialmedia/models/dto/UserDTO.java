package rongxchen.socialmedia.models.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author CHEN Rongxin
 * for login and signup
 */
@Data
public class UserDTO {

	@Email
	@NotNull
	private String email;

	@Size(max = 100, min = 1)
	private String username;

	@Size(max = 512, min = 8)
	@NotNull
	private String password;

}
