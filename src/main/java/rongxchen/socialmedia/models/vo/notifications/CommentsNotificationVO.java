package rongxchen.socialmedia.models.vo.notifications;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author CHEN Rongxin
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CommentsNotificationVO extends NotificationVO {

	private Boolean isAuthor;

	private String fromUsername;

	private String fromUserAvatar;

	private String postId;

	private String postTitle;

	private String parentId;

	private String commentContent;

}
