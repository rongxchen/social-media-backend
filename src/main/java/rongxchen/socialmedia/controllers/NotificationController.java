package rongxchen.socialmedia.controllers;

import org.springframework.web.bind.annotation.*;
import rongxchen.socialmedia.common.annotations.LoginToken;
import rongxchen.socialmedia.models.Result;
import rongxchen.socialmedia.models.vo.notifications.CommentsNotificationVO;
import rongxchen.socialmedia.models.vo.notifications.FollowsNotificationVO;
import rongxchen.socialmedia.service.NotificationService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author CHEN Rongxin
 */
@RestController
@RequestMapping("/api/notifications")
@LoginToken
public class NotificationController {

	@Resource
	private NotificationService notificationService;

	@GetMapping("/comments")
	public Result<List<CommentsNotificationVO>> getCommentsNotifications(@RequestParam("skip") long skip,
																		 @RequestParam(value = "limit", required = false, defaultValue = "20") long limit,
																		 @RequestAttribute("appId") String appId) {
		List<CommentsNotificationVO> notificationList = notificationService.getCommentsNotificationList(appId, skip, limit);
		return Result.success(notificationList);
	}

	@GetMapping("/follows")
	public Result<List<FollowsNotificationVO>> getFollowsNotifications(@RequestParam("skip") long skip,
																	   @RequestParam(value = "limit", required = false, defaultValue = "20") long limit,
																	   @RequestAttribute("appId") String appId) {
		List<FollowsNotificationVO> notificationList = notificationService.getFollowsNotificationList(appId, skip, limit);
		return Result.success(notificationList);
	}

	@PutMapping("/read-all")
	public Result<Boolean> readNotifications(@RequestParam("ids") String ids,
											 @RequestAttribute("appId") String appId) {
		notificationService.readList(appId, ids);
		return Result.success(true);
	}

	@DeleteMapping("/clear-all")
	public Result<Boolean> clearNotifications(@RequestParam("ids") String ids,
											  @RequestAttribute("appId") String appId) {
		notificationService.clearAll(appId, ids);
		return Result.success(true);
	}

}
