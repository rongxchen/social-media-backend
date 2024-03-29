package rongxchen.socialmedia.utils;

/**
 * @author CHEN Rongxin
 */
public class RandomCodeGenerator {

	private RandomCodeGenerator() {}

	public static String generateVerificationCode() {
		return generateNumeric(6);
	}

	public static String generateNumeric(int length) {
		String numerics = "0123456789";
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < length; i++) {
			int d = (int) (Math.random() * numerics.length());
			builder.append(numerics.charAt(d));
		}
		return builder.toString();
	}

	public static String generateMix(int length) {
		String mix = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < length; i++) {
			int d = (int) (Math.random() * mix.length());
			builder.append(mix.charAt(d));
		}
		return builder.toString();
	}

}
