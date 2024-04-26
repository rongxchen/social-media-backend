package rongxchen.socialmedia.controllers;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rongxchen.socialmedia.common.annotations.LoginToken;
import rongxchen.socialmedia.models.Result;
import rongxchen.socialmedia.models.dto.UserDTO;
import rongxchen.socialmedia.models.vo.UserVO;
import rongxchen.socialmedia.service.UserService;

import javax.annotation.Resource;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

/**
 * @author CHEN Rongxin
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

	@Resource
	private UserService userService;

	@PostMapping("/send-verification-code")
	public Result<Boolean> sendVerificationCode(@RequestBody UserDTO userDTO) {
		userService.sendVerificationCode(userDTO.getEmail());
		return Result.success(true, "verification code sent");
	}

	@PostMapping("/send-forgot-password")
	public Result<Boolean> sendForgotPassword(@RequestBody UserDTO userDTO) {
		userService.sendResetPassword(userDTO.getEmail());
		return Result.success(true, "email sent, check your inbox");
	}

	@PostMapping("/register")
	public Result<Boolean> register(@RequestBody @Validated UserDTO userDTO,
									@RequestParam @Size(min = 6, max = 6) String code) {
		userService.signUp(userDTO, code);
		return Result.success(true);
	}

	@PostMapping("/login")
	public Result<Map<String, Object>> login(@RequestBody @Validated UserDTO userDTO) {
		Map<String, Object> data = userService.login(userDTO);
		return Result.success(data);
	}

	@GetMapping
	@LoginToken
	public Result<UserVO> getUserInfo(@RequestParam("userId") String userId) {
		UserVO userInfo = userService.getUserInfo(userId);
		return Result.success(userInfo);
	}

	@PutMapping
	@LoginToken
	public Result<Boolean> updateUserInfo(@RequestAttribute String appId,
										  @RequestBody @Validated UserVO userVO) {
		userService.updateUser(appId, userVO);
		return Result.success(true);
	}

	@DeleteMapping
	@LoginToken
	public Result<Boolean> deleteUser(@RequestAttribute String appId) {
		userService.deleteUser(appId);
		return Result.success(true);
	}

	@GetMapping("/refresh-token")
	public Result<Map<String, String>> refreshToken(@RequestHeader("refresh-token") String refreshToken) {
		Map<String, String> map = userService.refreshToken(refreshToken);
		return Result.success(map);
	}

	@PostMapping("/friends")
	@LoginToken
	public Result<Boolean> collectFriends(@RequestParam("action") String action,
										  @RequestParam("friendId") String friendId,
										  @RequestAttribute String appId) {
		boolean success = userService.collectFriend(action, friendId, appId);
		return Result.success(success);
	}

	@GetMapping("/friends")
	@LoginToken
	public Result<Map<String, List<String>>> getFriendIdList(@RequestAttribute String appId) {
		Map<String, List<String>> friendList = userService.getFriendIdList(appId);
		return Result.success(friendList);
	}

	@GetMapping("/friends/count")
	@LoginToken
	public Result<Map<String, Integer>> getFriendsCount(@RequestParam("userId") String userId) {
		Map<String, Integer> friendsCount = userService.getFriendsCount(userId);
		return Result.success(friendsCount);
	}

	@GetMapping("/friends/follows")
	@LoginToken
	public Result<List<UserVO.SimpleUserVO>> getFollows(@RequestParam("offset") Integer offset,
														@RequestAttribute String appId) {
		List<UserVO.SimpleUserVO> followsList = userService.getFollowsList(appId, offset);
		return Result.success(followsList);
	}

	@GetMapping("/friends/followers")
	@LoginToken
	public Result<List<UserVO.SimpleUserVO>> getFollowers(@RequestParam("offset") Integer offset,
														@RequestAttribute String appId) {
		List<UserVO.SimpleUserVO> followersList = userService.getFollowersList(appId, offset);
		return Result.success(followersList);
	}

}
