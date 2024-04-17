package rongxchen.socialmedia.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author CHEN Rongxin
 */
@AllArgsConstructor
@Getter
public enum RedisKey implements CodeEnhancer {

	VERIFICATION_CODE("verification-code:");

	private final String code;

}
