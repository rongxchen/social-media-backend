package rongxchen.socialmedia.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * @author CHEN Rongxin
 */
@Getter
@AllArgsConstructor
public enum UserRole implements Serializable {

	ADMIN("admin"),

	USER("user");

	private final String role;

}
