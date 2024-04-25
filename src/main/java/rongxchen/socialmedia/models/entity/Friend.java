package rongxchen.socialmedia.models.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * @author CHEN Rongxin
 */
@Data
@Document(collection = "friends")
public class Friend {

	@Id
	private String id;

	private String friendId;

	private String followedByUserId;

	private LocalDateTime followedTime;

}
