package rongxchen.socialmedia.models.entity.notifications;

import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import rongxchen.socialmedia.enums.NotificationCategory;

/**
 * @author CHEN Rongxin
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Document(collection = "notifications")
public class CommentsNotification extends Notification {

    private Boolean isAuthor;

    private String postId;

    private String parentId;

    private String commentId;

    private String notificationCategory = NotificationCategory.COMMENTS.getCategory();

}
