package rongxchen.socialmedia.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author CHEN Rongxin
 */
@Getter
@AllArgsConstructor
public enum UserRole implements CodeEnhancer {

	ADMIN("admin"),

	USER("user");

	private final String code;

}
