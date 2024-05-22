package rongxchen.socialmedia.models.entity.notifications;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import rongxchen.socialmedia.enums.NotificationCategory;

/**
 * @author CHEN Rongxin
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Document(collection = "notifications")
public class FollowsNotification extends Notification {

	private String appId;

	private String notificationCategory = NotificationCategory.FOLLOWS.getCategory();
    
}
