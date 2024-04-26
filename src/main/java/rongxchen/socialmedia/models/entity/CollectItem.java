package rongxchen.socialmedia.models.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author CHEN Rongxin
 */
@Document(collection = "collect_items")
@Data
public class CollectItem {

	@Id
	private ObjectId id;

	private String userId;

	private String itemId;

	private String itemMeta;

	private String itemType;

	private String itemOwnerId;

	private LocalDateTime time;

}
