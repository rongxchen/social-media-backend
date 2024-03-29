package rongxchen.socialmedia.common;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import rongxchen.socialmedia.common.annotations.LoginToken;
import rongxchen.socialmedia.exceptions.HttpException;
import rongxchen.socialmedia.utils.JwtUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author CHEN Rongxin
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(@NotNull HttpServletRequest request,
							 @NotNull HttpServletResponse response,
							 @NotNull Object handler) {
		if (!(handler instanceof HandlerMethod)) {
			return true;
		}
		Method method = ((HandlerMethod) handler).getMethod();
		if (method.isAnnotationPresent(LoginToken.class)) {
			LoginToken loginToken = method.getAnnotation(LoginToken.class);
			if (loginToken.required()) {
				String token = request.getHeader("Authorization");
				if (token == null || !token.startsWith("Bearer ")) {
					throw new HttpException(HttpStatus.BAD_REQUEST, "no token");
				}
				token = token.replace("Bearer ", "");
				Map<String, String> claims = JwtUtil.decodeToken(token);
				if (!claims.getOrDefault("role", "").equals(loginToken.role())) {
					throw new HttpException(HttpStatus.UNAUTHORIZED, "not authorized to perform this action");
				}
				if (claims.containsKey("appId")) {
					request.setAttribute("appId", claims.getOrDefault("appId", ""));
					return true;
				}
				throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "error decoding token");
			}
			return true;
		}
		return true;
	}

}

