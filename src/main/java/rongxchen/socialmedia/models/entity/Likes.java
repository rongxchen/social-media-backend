package rongxchen.socialmedia.models.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author CHEN Rongxin
 */
@Document(collection = "likes")
@Data
public class Likes {

	private String userId;

	private String likedItemId;

	private String type;

}
