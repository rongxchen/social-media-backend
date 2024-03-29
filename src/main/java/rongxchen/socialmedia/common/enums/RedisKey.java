package rongxchen.socialmedia.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * @author CHEN Rongxin
 */
@AllArgsConstructor
@Getter
public enum RedisKey implements Serializable {

	VERIFICATION_CODE("verification-code:");

	private final String key;

}
