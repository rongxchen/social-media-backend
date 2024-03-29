package rongxchen.socialmedia.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import rongxchen.socialmedia.models.entity.User;

/**
 * @author CHEN Rongxin
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

	@Query("{ 'email' : ?0 }")
	User getByEmail(String email);

	@Query("{ 'appId' :  ?0 }")
	User getByAppId(String appId);

}
