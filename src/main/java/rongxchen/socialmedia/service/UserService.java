package rongxchen.socialmedia.service;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rongxchen.socialmedia.enums.RedisKey;
import rongxchen.socialmedia.enums.UserRole;
import rongxchen.socialmedia.exceptions.AccountException;
import rongxchen.socialmedia.exceptions.HttpException;
import rongxchen.socialmedia.models.dto.UserDTO;
import rongxchen.socialmedia.models.entity.Friend;
import rongxchen.socialmedia.models.entity.User;
import rongxchen.socialmedia.models.vo.UserVO;
import rongxchen.socialmedia.repository.FriendRepository;
import rongxchen.socialmedia.repository.UserRepository;
import rongxchen.socialmedia.service.azure.AzureBlobService;
import rongxchen.socialmedia.service.azure.AzureMailService;
import rongxchen.socialmedia.service.common.MyMongoService;
import rongxchen.socialmedia.utils.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author CHEN Rongxin
 */
@Service
@Transactional
public class UserService {

	@Resource
	private UserRepository userRepository;

	@Resource
	private FriendRepository friendRepository;

	@Resource
	private MongoTemplate mongoTemplate;

	@Resource
	private MyMongoService myMongoService;

	@Resource
	private AzureMailService azureMailService;

	@Resource
	private AzureBlobService azureBlobService;

	@Resource
	private NotificationService notificationService;

	@Value("${spring.cloud.azure.storage.blob.end-point}")
	private String BLOB_URL_PREFIX;

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
		Query query = new Query(Criteria.where("itemKey").is(RedisKey.VERIFICATION_CODE.getCode() + userDto.getEmail()));
		List<Document> documents = mongoTemplate.find(query, Document.class, "sundries");
		String findCode = documents.isEmpty() ? null : documents.get(0).getString("code");
		if (findCode == null) {
			throw new AccountException("code expired");
		}
		if (!findCode.equals(code)) {
			throw new AccountException("code unmatched");
		}
		// remove code from redis
		mongoTemplate.remove(documents.get(0), "sundries");
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
		if (user.getBirthday() != null) {
			userVo.setBirthday(user.getBirthday().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		}
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

	public UserVO getUserInfo(String appId) {
		User user = userRepository.getByAppId(appId);
		UserVO userVO = new UserVO();
		userVO.setAppId(user.getAppId());
		userVO.setUsername(user.getUsername());
		userVO.setAvatar(user.getAvatar());
		userVO.setSex(user.getSex());
		if (user.getBirthday() != null) {
			userVO.setAge((int) ChronoUnit.YEARS.between(user.getBirthday(), LocalDate.now()));
		}
		userVO.setDescription(user.getDescription());
		return userVO;
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
		if (userVO.getBirthday() != null && !userVO.getBirthday().isEmpty()) {
			LocalDate birthday = LocalDate.parse(userVO.getBirthday(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			if (birthday.isAfter(LocalDate.now())) {
				throw new RuntimeException("birthday cannot be in the future");
			}
			user.setBirthday(birthday);
		}
		user.setSex(userVO.getSex());
		user.setDescription(userVO.getDescription());
		userRepository.save(user);
	}

	@SuppressWarnings("null")
	public String uploadAvatar(String appId, MultipartFile file) {
		User user = userRepository.getByAppId(appId);
		if (user == null) {
			throw new RuntimeException("user not found");
		}
		String suffix = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf("."));
		String blobName = "users/avatar/" + UUIDGenerator.generate(true) + suffix;
		try {
			azureBlobService.uploadFile("media", blobName, file.getInputStream(), file.getContentType());
		} catch (IOException e) {
			throw new RuntimeException("failed to upload avatar");
		}
		String newAvatar = BLOB_URL_PREFIX + "media/" + blobName;
		if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
			removeAvatar(appId);
		}
		user.setAvatar(newAvatar);
		userRepository.save(user);
		return newAvatar;
	}

	public void removeAvatar(String appId) {
		User user = userRepository.getByAppId(appId);
		if (user == null) {
			throw new RuntimeException("user not found");
		}
		if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
			String blobName = user.getAvatar().replace(BLOB_URL_PREFIX + "media/", "");
			azureBlobService.removeFile("media", blobName);
		}
		user.setAvatar("");
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
		removeAvatar(appId);
		userRepository.save(newUser);
	}

	public void sendVerificationCode(String email) {
		// find if email has been registered
		User user = userRepository.getByEmail(email);
		if (user != null) {
			throw new AccountException("email has been registered");
		}
		// find if the code existed already
		Query query = new Query(Criteria.where("itemKey").is(RedisKey.VERIFICATION_CODE.getCode() + email));
		List<Document> documents = mongoTemplate.find(query, Document.class, "sundries");
		String findCode = documents.isEmpty() ? "" : documents.get(0).getString("code");
		if (findCode != null && !findCode.isEmpty()) {
			throw new RuntimeException("verification code has been sent, please check your email");
		}
		// generate verification code and store in redis
		String code = RandomCodeGenerator.generateVerificationCode();
		Document document = new Document();
		document.put("itemKey", RedisKey.VERIFICATION_CODE.getCode() + email);
		document.put("code", code);
		mongoTemplate.save(document, "sundries");
		azureMailService.sendVerificationCode(email, code);
	}

	public void sendResetPassword(String email) {
		User user = userRepository.getByEmail(email);
		if (user == null) {
			throw new AccountException("no such user");
		}
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

	public boolean collectFriend(String action, String friendId, String myId) {
		Friend findFriend = friendRepository.getFriend(myId, friendId);
		if ("follow".equals(action)) {
			if (findFriend != null) {
				throw new RuntimeException("followed already");
			}
			Friend friend = new Friend();
			friend.setFollowedTime(LocalDateTime.now());
			friend.setFriendId(friendId);
			friend.setFollowedByUserId(myId);
			friendRepository.save(friend);
			System.out.println("sending comment notification: " + friend);
			notificationService.sendFollowsNotification(friend);
		} else if ("unfollow".equals(action)) {
			if (findFriend != null) {
				friendRepository.delete(findFriend);
			}
		} else {
			return false;
		}
		return true;
	}

	public Map<String, Integer> getFriendsCount(String userId) {
		Map<String, Integer> countMap = new HashMap<>();
		// count follows
		Friend following = new Friend();
		following.setFollowedByUserId(userId);
		Example<Friend> followsCountExample = Example.of(following);
		countMap.put("follows", (int) friendRepository.count(followsCountExample));
		// count followers
		following.setFollowedByUserId(null);
		following.setFriendId(userId);
		Example<Friend> followersCountExample = Example.of(following);
		countMap.put("followers", (int) friendRepository.count(followersCountExample));
		return countMap;
	}

	public Map<String, List<String>> getFriendIdList(String userId) {
		Map<String, List<String>> friendList = new HashMap<>();
		// find follows
		friendList.put("follows",
				friendRepository.getMyFollows(userId)
						.stream().map(Friend::getFriendId)
						.collect(Collectors.toList()));
		// find followers
		friendList.put("followers",
				friendRepository.getMyFollowers(userId)
						.stream().map(Friend::getFollowedByUserId)
						.collect(Collectors.toList()));
		return friendList;
	}

	public List<UserVO.SimpleUserVO> getFollowsList(String userId, int offset) {
		int defaultSize = 20;
		return myMongoService.lookup("users", "appId", "friendId", "friendInfo")
				.unwind("friendInfo")
				.match(Criteria.where("followedByUserId").is(userId))
				.project("friendInfo.username as username", "friendInfo.avatar as avatar", "friendId as appId")
				.skip(offset)
				.limit(defaultSize)
				.fetchResult("friends", UserVO.SimpleUserVO.class);
	}

	public List<UserVO.SimpleUserVO> getFollowersList(String userId, int offset) {
		int defaultSize = 20;
		return myMongoService.lookup("users", "appId", "followedByUserId", "friendInfo")
				.unwind("friendInfo")
				.match(Criteria.where("friendId").is(userId))
				.project("friendInfo.username as username", "friendInfo.avatar as avatar", "followedByUserId as appId")
				.skip(offset)
				.limit(defaultSize)
				.fetchResult("friends", UserVO.SimpleUserVO.class);
	}

}
