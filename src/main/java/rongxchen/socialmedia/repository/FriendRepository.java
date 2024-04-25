package rongxchen.socialmedia.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import rongxchen.socialmedia.models.entity.Friend;

import java.util.List;

/**
 * @author CHEN Rongxin
 */
@Repository
public interface FriendRepository extends MongoRepository<Friend, String> {

	@Query("{ 'followedByUserId' : ?0, 'friendId': ?1 }")
	Friend getFriend(String userId, String friendId);

	@Query("{ 'followedByUserId' : ?0 }")
	List<Friend> getMyFollows(String userId);

	@Query("{ 'friendId': ?0 }")
	List<Friend> getMyFollowers(String userId);

}
