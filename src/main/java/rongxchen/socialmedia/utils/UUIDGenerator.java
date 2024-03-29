package rongxchen.socialmedia.utils;

import java.util.UUID;

/**
 * @author CHEN Rongxin
 */
public class UUIDGenerator {

	private UUIDGenerator() {}

	public static String generate(boolean withoutDash) {
		UUID uuid = UUID.randomUUID();
		if (withoutDash) {
			return uuid.toString().replace("-", "");
		}
		return uuid.toString();
	}

	public static String generate(String prefix, boolean withoutDash) {
		return prefix + "-" + generate(withoutDash);
	}

	public static String generate(String prefix) {
		return prefix + "-" + generate(false);
	}

}
