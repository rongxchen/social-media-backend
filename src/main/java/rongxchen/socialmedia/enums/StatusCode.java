package rongxchen.socialmedia.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author CHEN Rongxin
 */
@AllArgsConstructor
@Getter
public enum StatusCode {

	OK(0),

	FAIL(1);

	private final Integer code;

}
