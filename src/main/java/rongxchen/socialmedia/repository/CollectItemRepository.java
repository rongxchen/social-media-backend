package rongxchen.socialmedia.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import rongxchen.socialmedia.models.entity.CollectItem;

import java.util.List;

/**
 * @author CHEN Rongxin
 */
@Repository
public interface CollectItemRepository extends MongoRepository<CollectItem, String> {

	@Query("{ 'itemId' : ?0 , 'itemType': ?1 'userId' : ?2 }")
	CollectItem getByItemId(String itemId, String itemType, String userId);

	@Query("{ 'userId' : ?0 }")
	List<CollectItem> getByUserId(String userId);

}
