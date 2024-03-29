package rongxchen.socialmedia.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.http.HttpStatus;
import rongxchen.socialmedia.exceptions.HttpException;
import rongxchen.socialmedia.exceptions.UnauthorizedException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * @author CHEN Rongxin
 */
public class JwtUtil {

	private JwtUtil() {}

	private static final int MILLIS_PER_MINUTE = 60;

	private static final String SEC_KEY = EncryptionUtil.calculateSHA256("socialmedia2024");

	public static String generateToken(Map<String, String> payloads, int minutes) {
		Instant exp = Instant.ofEpochSecond(Instant.now().getEpochSecond() + (long) MILLIS_PER_MINUTE * minutes);
		return JWT.create()
				.withClaim("appId", payloads.getOrDefault("appId", null))
				.withClaim("role", payloads.getOrDefault("role", null))
				.withExpiresAt(exp)
				.sign(Algorithm.HMAC256(SEC_KEY));
	}

	public static String generateToken(Map<String, String> payloads) {
		return generateToken(payloads, 1);
	}

	public static Map<String, String> decodeToken(String token) {
		try {
			JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SEC_KEY)).build();
			verifier.verify(token);
			DecodedJWT decode = JWT.decode(token);
			Instant expiresAt = decode.getExpiresAtAsInstant();
			if (expiresAt.isBefore(Instant.ofEpochSecond(Instant.now().getEpochSecond()))) {
				throw new HttpException(HttpStatus.UNAUTHORIZED, "token expired");
			}
			Map<String, String> decoded = new HashMap<>();
			decode.getClaims().forEach((k, v) -> decoded.put(k, v.asString()));
			return decoded;
		} catch (JWTVerificationException e) {
			throw new UnauthorizedException("token expired");
		}
	}

}
