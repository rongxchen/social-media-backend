package rongxchen.socialmedia.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import rongxchen.socialmedia.models.Result;

/**
 * @author CHEN Rongxin
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(RuntimeException.class)
	public Result<String> handleException(RuntimeException exception) {
		return Result.fail(exception.getMessage());
	}

	@ExceptionHandler(HttpException.class)
	public Result<String> handleHttpException(HttpException exception) {
		switch (exception.getStatus()) {
			case BAD_REQUEST -> {
				return new Result<>(HttpStatus.BAD_REQUEST.value(), null, exception.getMessage());
			}
			case UNAUTHORIZED -> {
				return new Result<>(HttpStatus.UNAUTHORIZED.value(), null, exception.getMessage());
			}
			default -> {
				return Result.fail(exception.getMessage());
			}
		}
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<String> handleUnauthorizedException(UnauthorizedException exception) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public Result<String> handleMissingServletRequestParameterException(MissingServletRequestParameterException exception) {
		return Result.fail(exception.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public Result<String> handleException(Exception exception) {
		return Result.fail(exception.getMessage());
	}

}
