package rongxchen.socialmedia.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rongxchen.socialmedia.enums.RedisKey;
import rongxchen.socialmedia.enums.UserRole;
import rongxchen.socialmedia.message_queue.RocketMQProducer;
import rongxchen.socialmedia.exceptions.AccountException;
import rongxchen.socialmedia.exceptions.HttpException;
import rongxchen.socialmedia.models.dto.UserDTO;
import rongxchen.socialmedia.models.entity.User;
import rongxchen.socialmedia.models.mq.MQBody;
import rongxchen.socialmedia.models.vo.UserVO;
import rongxchen.socialmedia.repository.RedisRepository;
import rongxchen.socialmedia.repository.UserRepository;
import rongxchen.socialmedia.service.azure.AzureMailService;
import rongxchen.socialmedia.utils.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * @author CHEN Rongxin
 */
@Service
@Transactional
public class UserService {

	@Resource
	private UserRepository userRepository;

	@Resource
	private RedisRepository redisRepository;

	// TODO
//	@Resource
//	RocketMQProducer rocketMQProducer;

	@Resource
	private AzureMailService azureMailService;

	public void signUp(UserDTO userDto, String code) {
		// find if user has registered before
		User findUser = userRepository.getByEmail(userDto.getEmail());
		User user = new User();
		if (findUser != null) {
			if (findUser.getDeleted() == 0) {
				throw new AccountException("email has been registered");
			}
			user.setId(findUser.getId());
			user.setAppId(findUser.getAppId());
		}
		// check if code is correct
		String findCode = redisRepository.get(RedisKey.VERIFICATION_CODE.getCode(), userDto.getEmail());
		if (findCode == null) {
			throw new AccountException("code expired");
		}
		if (!findCode.equals(code)) {
			throw new AccountException("code unmatched");
		}
		// set up basic info
		user.setEmail(userDto.getEmail());
		user.setUsername(userDto.getUsername());
		// set up app id and other info
		if (user.getAppId() == null) {
			String appId = UUIDGenerator.generate(true);
			while (userRepository.getByAppId(appId) != null) {
				appId = UUIDGenerator.generate(true);
			}
			user.setAppId(appId);
		}
		String salt = EncryptionUtil.calculateSHA256(LocalDate.now().toString());
		String password = EncryptionUtil.calculateSHA256(userDto.getPassword() + salt);
		user.setSalt(salt);
		user.setPassword(password);
		user.setAvatar("");
		user.setStatus(1);
		user.setDeleted(0);
		user.setRole(UserRole.USER.getCode());
		user.setCreateTime(LocalDateTime.now());
		user.setUpdateTime(LocalDateTime.now());
		userRepository.save(user);
	}

	private void checkUserValidation(User user) {
		if (user == null || user.getDeleted() == 1) {
			throw new AccountException("wrong email or password");
		}
		if (user.getStatus() == 0) {
			throw new AccountException("account has been disabled");
		}
	}

	public Map<String, Object> login(UserDTO userDTO) {
		User user = userRepository.getByEmail(userDTO.getEmail());
		checkUserValidation(user);
		String _password = EncryptionUtil.calculateSHA256(userDTO.getPassword() + user.getSalt());
		if (!_password.equals(user.getPassword())) {
			throw new AccountException("wrong email or password");
		}
		UserVO userVo = new UserVO();
		userVo.setUsername(user.getUsername());
		userVo.setAppId(user.getAppId());
		userVo.setDescription(user.getDescription());
		userVo.setBirthday(user.getBirthday().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		userVo.setEmail(user.getEmail());
		userVo.setSex(user.getSex());
		userVo.setAvatar(user.getAvatar());
		userVo.setCreateTime(DateUtil.convertDateTimeToString(user.getCreateTime()));
		userVo.setUpdateTime(DateUtil.convertDateTimeToString(user.getUpdateTime()));
		// create tokens for user if login successfully
		HashMap<String, String> claims = new HashMap<>();
		claims.put("appId", user.getAppId());
		claims.put("role", user.getRole());
		String accessToken = JwtUtil.generateToken(claims);
		String refreshToken = JwtUtil.generateToken(claims, 60 * 24 * 7);
		Map<String, Object> map = new HashMap<>();
		map.put("userInfo", userVo);
		map.put("accessToken", accessToken);
		map.put("refreshToken", refreshToken);
		return map;
	}

	private void checkUserExists(User user) {
		if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
			throw new AccountException("no such user");
		}
	}

	public void updateUser(String appId, UserVO userVO) {
		User user = userRepository.getByAppId(appId);
		checkUserExists(user);
		if (!user.getUsername().equals(userVO.getUsername())) {
			if (ChronoUnit.DAYS.between(LocalDate.now(), user.getUpdateTime()) < 15) {
				throw new AccountException("username can only be updated once every 15 days");
			}
			user.setUsername(userVO.getUsername());
			user.setUpdateTime(LocalDateTime.now());
		}
		user.setBirthday(LocalDateTime.parse(userVO.getBirthday(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		user.setSex(userVO.getSex());
		user.setDescription(userVO.getDescription());
		userRepository.save(user);
	}

	public void deleteUser(String appId) {
		User user = userRepository.getByAppId(appId);
		checkUserExists(user);
		User newUser = new User();
		newUser.setId(user.getId());
		newUser.setAppId(appId);
		newUser.setEmail(user.getEmail());
		newUser.setDeleted(1);
		userRepository.save(newUser);
	}

	public void sendVerificationCode(String email) {
		// find if email has been registered
		User user = userRepository.getByEmail(email);
		if (user != null) {
			throw new AccountException("email has been registered");
		}
		// generate verification code and store in redis
		String code = RandomCodeGenerator.generateVerificationCode();
		redisRepository.setItem(RedisKey.VERIFICATION_CODE.getCode(), email, code, 60 * 10);
		// set message meta for mq
		// TODO
//		MQBody mqBody = new MQBody("mail_verification_code");
//		mqBody.add("email", email);
//		mqBody.add("code", code);
//		rocketMQProducer.sendMessage("azure-mail", mqBody);

		azureMailService.sendVerificationCode(email, code);
	}

	public void sendResetPassword(String email) {
		User user = userRepository.getByEmail(email);
		if (user == null) {
			throw new AccountException("no such user");
		}
		// set message meta for mq
		// TODO
//		MQBody mqBody = new MQBody("mail_reset_password");
//		mqBody.add("email", email);
//		mqBody.add("appId", user.getAppId());
//		mqBody.add("username", user.getUsername());
//		rocketMQProducer.sendMessage("azure-mail", mqBody);

		azureMailService.sendResetPassword(user.getAppId(), user.getUsername(), email);
	}

	public Map<String, String> refreshToken(String refreshToken) {
		if (refreshToken == null || !refreshToken.startsWith("Bearer ")) {
			throw new HttpException(HttpStatus.BAD_REQUEST, "invalid token");
		}
		refreshToken = refreshToken.replace("Bearer ", "");
		// check refresh token
		Map<String, String> decoded = JwtUtil.decodeToken(refreshToken);
		String appId = decoded.getOrDefault("appId", "");
		// check user status
		User user = userRepository.getByAppId(appId);
		checkUserValidation(user);
		// generate new access token
		Map<String, String> payload = new HashMap<>();
		payload.put("role", user.getRole());
		payload.put("appId", user.getAppId());
		String token = JwtUtil.generateToken(payload, 30);
		// return token
		Map<String, String> data = new HashMap<>();
		data.put("accessToken", token);
		return data;
	}

}
