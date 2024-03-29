package rongxchen.socialmedia.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import rongxchen.socialmedia.common.LoginInterceptor;

/**
 * @author CHEN Rongxin
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new LoginInterceptor())
				.addPathPatterns("/**")
				.excludePathPatterns(
						"/api/users/login",
						"/api/users/register",
						"/api/users/send-verification-code",
						"/api/users/send-forgot-password"
				);
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowCredentials(true)
				.allowedOriginPatterns("*")
				.allowedMethods("*")
				.allowedHeaders("*")
				.exposedHeaders("*");
	}

}
