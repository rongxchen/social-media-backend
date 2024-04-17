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

	public static <T> Result<T> success(T data, String message) {
		return new Result<>(StatusCode.OK.getCode(), data, message);
	}

	public static <T> Result<T> success(T data) {
		return Result.success(data, "success");
	}

	public static <T> Result<T> error(String message) {
		return new Result<>(StatusCode.FAIL.getCode(), null, message);
	}

}
