package rongxchen.socialmedia.repository.notifications;

import org.springframework.data.mongodb.repository.MongoRepository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import rongxchen.socialmedia.models.entity.notifications.CommentsNotification;

import java.util.List;

@Repository
public interface CommentsNotificationRepository extends MongoRepository<CommentsNotification, String> {

	@Query(" { 'notificationId': ?0, 'notificationCategory': ?1 } ")
	CommentsNotification findOne(String notificationId, String notificationCategory);

	@Query(" { 'notificationId' : { $in : ?0, 'notificationCategory': ?1 } } ")
	void deleteBatchByNotificationId(List<String> idList, String notificationCategory);

}
