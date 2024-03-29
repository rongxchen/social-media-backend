package rongxchen.socialmedia.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import rongxchen.socialmedia.enums.StatusCode;

/**
 * @author CHEN Rongxin
 */
@Data
@AllArgsConstructor
public class Result<T> {

	private Integer code;

	private T data;

	private String message;

	public static <T> Result<T> ok(T data, String message) {
		return new Result<>(StatusCode.OK.getCode(), data, message);
	}

	public static <T> Result<T> ok(T data) {
		return Result.ok(data, "success");
	}

	public static <T> Result<T> ok() {
		return Result.ok(null, "success");
	}

	public static <T> Result<T> fail(String message) {
		return new Result<>(StatusCode.FAIL.getCode(), null, message);
	}

	public static <T> Result<T> fail() {
		return Result.fail("failed");
	}

}
