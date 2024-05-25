package rongxchen.socialmedia.models.vo.notifications;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author CHEN Rongxin
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LikesNotificationVO extends NotificationVO {

	private String fromUsername;

	private String fromUserAvatar;

	private String postId;

	private String action;

	private String itemType;

	private String itemId;

	private String content;

}
