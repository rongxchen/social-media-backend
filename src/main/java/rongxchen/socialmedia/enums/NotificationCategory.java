package rongxchen.socialmedia.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author CHEN Rongxin
 */
@Getter
@AllArgsConstructor
public enum NotificationCategory {

	LIKES("likes"),

	COMMENTS("comments"),

	FOLLOWS("follows");

	private final String category;

}
