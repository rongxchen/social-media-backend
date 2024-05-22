package rongxchen.socialmedia.models.vo.notifications;

import lombok.Data;

/**
 * @author CHEN Rongxin
 */
@Data
public class NotificationVO {

	private String notificationId;

	private String fromUserId;

	private String toUserId;

	private String dateTime;

	private Boolean read;

}
