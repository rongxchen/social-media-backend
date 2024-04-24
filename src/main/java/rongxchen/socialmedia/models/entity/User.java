package rongxchen.socialmedia.models.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * @author CHEN Rongxin
 * user entity stored in database
 */
@Data
@Document(collection = "users")
public class User {

	@Id
	private String id;

	@Size(max = 50, min = 8)
	private String username;

	@Email
	private String email;

	@Size(max = 30, min = 8)
	private String password;

	private String salt;

	private String appId;

	@Size(max = 500)
	private String description;

	private String sex;

	private String avatar;

	private LocalDateTime birthday;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;

	private String role;

	private Integer status;

	private Integer deleted;

}
