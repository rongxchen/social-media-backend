package rongxchen.socialmedia.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import rongxchen.socialmedia.models.entity.Comment;

/**
 * @author CHEN Rongxin
 */
@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {

	@Query("{ 'commentId' : ?0 }")
	Comment getByCommentId(String commentId);

}
