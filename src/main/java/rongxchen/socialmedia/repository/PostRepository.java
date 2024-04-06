package rongxchen.socialmedia.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import rongxchen.socialmedia.models.entity.Post;

/**
 * @author CHEN Rongxin
 */
@Repository
public interface PostRepository extends MongoRepository<Post, String> {

	@Query("{ 'postId' : ?0 }")
	Post getByPostId(String postId);

}
