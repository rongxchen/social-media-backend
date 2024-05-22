package rongxchen.socialmedia.repository.notifications;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import rongxchen.socialmedia.models.entity.notifications.FollowsNotification;

import java.util.List;

/**
 * @author CHEN Rongxin
 */
@Repository
public interface FollowsNotificationRepository extends MongoRepository<FollowsNotification, String> {

	@Query(" { 'notificationId': ?0, 'notificationCategory': ?1 } ")
	FollowsNotification findOne(String notificationId, String notificationCategory);

	@Query(" { 'notificationId' : { $in : ?0, 'notificationCategory': ?1 } } ")
	void deleteBatchByNotificationId(List<String> idList, String notificationCategory);


}
