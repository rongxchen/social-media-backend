package rongxchen.socialmedia.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import rongxchen.socialmedia.common.enums.RedisKey;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author CHEN Rongxin
 */
@Component
public class RedisRepository {

	@Resource
	RedisTemplate<String, Object> redisTemplate;

	public void set(RedisKey key, String id, String value) {
		redisTemplate.opsForValue().set(key.getKey() + id, value);
	}

	public void set(String key, String id, String value) {
		redisTemplate.opsForValue().set(key + id, value);
	}

	public void setWithTimeLimit(RedisKey key, String id, String value, long expSecond) {
		setWithTimeLimit(key.getKey(), id, value, expSecond);
	}

	public void setWithTimeLimit(String key, String id, String value, long expSecond) {
		redisTemplate.opsForValue().set(key + id, value, expSecond, TimeUnit.SECONDS);
	}

	public String get(RedisKey key, String id) {
		return get(key.getKey(), id);
	}

	public String get(String key, String id) {
		Object data = redisTemplate.opsForValue().get(key + id);
		return data == null ? null : data.toString();
	}

}
