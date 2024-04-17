package rongxchen.socialmedia.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author CHEN Rongxin
 */
@Component
public class RedisRepository {

	@Resource
	RedisTemplate<String, Object> redisTemplate;

	public void setItem(String topic, String hashKey, String value) {
		redisTemplate.opsForValue().set(topic + hashKey, value);
	}

	public void setItem(String topic, String hashKey, String value, long seconds) {
		redisTemplate.opsForValue()
				.set(topic + hashKey, value, seconds, TimeUnit.SECONDS);
	}

	public String get(String topic, String hashKey) {
		Object data = redisTemplate.opsForValue().get(topic + hashKey);
		return data == null ? null : data.toString();
	}

}
