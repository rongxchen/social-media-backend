package rongxchen.socialmedia.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * @author CHEN Rongxin
 */
@AllArgsConstructor
@Getter
public enum MessageType implements Serializable {

	MAIL_VERIFICATION_CODE("mail_verification_code"),

	MAIL_RESET_PASSWORD("mail_reset_password");

	public final String value;

}
