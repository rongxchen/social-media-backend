package rongxchen.socialmedia.controllers;

import org.springframework.web.bind.annotation.*;
import rongxchen.socialmedia.common.annotations.LoginToken;
import rongxchen.socialmedia.models.Result;
import rongxchen.socialmedia.models.dto.UserDTO;
import rongxchen.socialmedia.models.vo.UserVO;
import rongxchen.socialmedia.service.UserService;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
	public Result<Boolean> sendVerificationCode(@NotNull @RequestBody UserDTO userDTO) {
		userService.sendVerificationCode(userDTO.getEmail());
		return Result.ok(true, "verification code sent");
	}

	@PostMapping("/send-forgot-password")
	public Result<Boolean> sendForgotPassword(@NotNull @RequestBody UserDTO userDTO) {
		userService.sendResetPassword(userDTO.getEmail());
		return Result.ok(true, "email sent, check your inbox");
	}

	@PostMapping("/register")
	public Result<Boolean> register(@NotNull @RequestBody UserDTO userDTO,
									@NotNull @RequestParam @Size(min = 6, max = 6) String code) {
		userService.signUp(userDTO, code);
		return Result.ok(true);
	}

	@PostMapping("/login")
	public Result<Object> login(@NotNull @RequestBody UserDTO userDTO) {
		Map<String, Object> data = userService.login(userDTO);
		return Result.ok(data);
	}

	@PutMapping
	@LoginToken
	public Result<Boolean> updateUserInfo(@RequestAttribute String appId,
										  @NotNull @RequestBody UserVO userVO) {
		userService.updateUser(appId, userVO);
		return Result.ok(true);
	}

	@DeleteMapping
	@LoginToken
	public Result<Boolean> deleteUser(@RequestAttribute String appId) {
		userService.deleteUser(appId);
		return Result.ok(true);
	}

	@GetMapping
	public Result<Map<String, String>> refreshToken(@RequestHeader("refresh-token") String refreshToken) {
		Map<String, String> map = userService.refreshToken(refreshToken);
		return Result.ok(map);
	}

}
