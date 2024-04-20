package rongxchen.socialmedia.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import rongxchen.socialmedia.models.entity.Likes;

import java.util.List;

/**
 * @author CHEN Rongxin
 */
@Repository
public interface LikesRepository extends MongoRepository<Likes, String> {

	@Query("{ 'likedItemId' : ?0 , 'userId' : ?1}")
	Likes getByItemId(String likedItemId, String userId);

	@Query(" { 'userId' : ?0 } ")
	List<Likes> getByUserId(String userId);

}
