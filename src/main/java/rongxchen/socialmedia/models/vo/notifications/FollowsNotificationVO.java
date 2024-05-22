package rongxchen.socialmedia.models.vo.notifications;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author CHEN Rongxin
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FollowsNotificationVO extends NotificationVO {

	private String fromUsername;

	private String fromUserAvatar;

}
