package rongxchen.socialmedia.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

/**
 * @author CHEN Rongxin
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HttpException extends RuntimeException {

	private HttpStatus status;

	public HttpException(HttpStatus status, String message) {
		super(status.toString() + ": " + message);
		this.status = status;
	}

}
