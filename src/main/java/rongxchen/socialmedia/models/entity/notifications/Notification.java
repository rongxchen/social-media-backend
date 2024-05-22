package rongxchen.socialmedia.models.entity.notifications;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * @author CHEN Rongxin
 */
@Data
@Document(collection = "notifications")
public class Notification {

	@Id
	private String id;

	private String notificationId;

	private String fromUserId;

	private String toUserId;

	private LocalDateTime dateTime;

	private Boolean read = Boolean.FALSE;

}
