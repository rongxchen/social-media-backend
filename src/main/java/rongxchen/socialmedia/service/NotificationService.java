package rongxchen.socialmedia.service;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import rongxchen.socialmedia.enums.NotificationCategory;
import rongxchen.socialmedia.models.entity.Comment;
import rongxchen.socialmedia.models.entity.notifications.CommentsNotification;
import rongxchen.socialmedia.models.entity.notifications.Notification;
import rongxchen.socialmedia.models.vo.notifications.CommentsNotificationVO;
import rongxchen.socialmedia.repository.notifications.CommentsNotificationRepository;
import rongxchen.socialmedia.repository.notifications.FollowsNotificationRepository;
import rongxchen.socialmedia.repository.notifications.LikesNotificationRepository;
import rongxchen.socialmedia.service.common.MyMongoService;
import rongxchen.socialmedia.utils.DateUtil;
import rongxchen.socialmedia.utils.ObjectUtil;
import rongxchen.socialmedia.utils.UUIDGenerator;
import rongxchen.socialmedia.utils.WebsocketManager;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author CHEN Rongxin
 */
@Service
public class NotificationService {

	@Resource
	private LikesNotificationRepository likesNotificationRepository;

	@Resource
	private FollowsNotificationRepository followsNotificationRepository;

	@Resource
	private CommentsNotificationRepository commentsNotificationRepository;

	@Resource
	private ObjectUtil objectUtil;

	private static final String COMMENT_NOT_EXIST_EXPRESSION = "original comment has been deleted";

	@Resource
	private MyMongoService myMongoService;

	@Resource
	private MongoTemplate mongoTemplate;

	public Notification getOne(String notificationId, NotificationCategory category) {
		switch (category) {
			case LIKES: {
				return likesNotificationRepository.findOne(notificationId, category.getCategory());
			}
			case FOLLOWS: {
				return followsNotificationRepository.findOne(notificationId, category.getCategory());
			}
			case COMMENTS: {
				return commentsNotificationRepository.findOne(notificationId, category.getCategory());
			}
		}
		return null;
	}

	private String generateUniqueId(NotificationCategory category) {
		String id = UUIDGenerator.generate("notification");
		while (getOne(id, category) != null) {
			id = UUIDGenerator.generate("notification");
		}
		return id;
	}

	public void sendCommentsNotification(Comment comment, boolean isAuthor) {
		CommentsNotification notification = new CommentsNotification();
		notification.setNotificationId(generateUniqueId(NotificationCategory.COMMENTS));
		notification.setPostId(comment.getPostId());
		notification.setParentId(comment.getParentId());
		notification.setCommentId(comment.getCommentId());
		notification.setFromUserId(comment.getAuthorId());
		notification.setToUserId(comment.getReplyCommentUserId());
		notification.setIsAuthor(isAuthor);
		notification.setDateTime(LocalDateTime.now());
		commentsNotificationRepository.save(notification);
		CommentsNotificationVO commentsNotification = getCommentsNotification(notification.getNotificationId());
		WebsocketManager.sendTo(comment.getReplyCommentUserId(), objectUtil.write(commentsNotification));
	}

	public CommentsNotificationVO getCommentsNotification(String notificationId) {
		CommentsNotificationVO notification = myMongoService
				.match(Criteria.where("notificationId").is(notificationId)
						.and("notificationCategory").is(NotificationCategory.COMMENTS.getCategory())
				)
				.lookup("users", "appId", "fromUserId", "fromUser")
				.lookup("posts", "postId", "postId", "fromPost")
				.lookup("comments", "commentId", "commentId", "fromComment")
				.unwind("fromUser", true)
				.unwind("fromPost", true)
				.unwind("fromComment", true)
				.project("notificationId", "fromUserId", "toUserId", "dateTime", "read",
						"isAuthor", "fromUser.username as fromUsername", "fromUser.avatar as fromUserAvatar",
						"postId", "fromPost.title as postTitle", "parentId", "fromComment.content as commentContent")
				.fetchOne("notifications", CommentsNotificationVO.class);
		notification.setDateTime(DateUtil.convertToDisplayTime(notification.getDateTime()));
		if (notification.getCommentContent() == null) {
			notification.setCommentContent(COMMENT_NOT_EXIST_EXPRESSION);
		}
		return notification;
	}

	public List<CommentsNotificationVO> getCommentsNotificationList(String appId, long skip, long limit) {
		List<CommentsNotificationVO> notifications = myMongoService
				.match(Criteria.where("toUserId").is(appId)
						.and("notificationCategory").is(NotificationCategory.COMMENTS.getCategory()))
				.lookup("users", "appId", "fromUserId", "fromUser")
				.lookup("posts", "postId", "postId", "fromPost")
				.lookup("comments", "commentId", "commentId", "fromComment")
				.unwind("fromUser", true)
				.unwind("fromPost", true)
				.unwind("fromComment", true)
				.project("notificationId", "fromUserId", "toUserId", "dateTime", "read",
						"isAuthor", "fromUser.username as fromUsername", "fromUser.avatar as fromUserAvatar",
						"postId", "fromPost.title as postTitle", "parentId",  "fromComment.content as commentContent")
				.skip(skip)
				.limit(limit)
				.sort("dateTime", -1)
				.fetchResult("notifications", CommentsNotificationVO.class);
		for (CommentsNotificationVO notification : notifications) {
			notification.setDateTime(DateUtil.convertToDisplayTime(notification.getDateTime()));
			if (notification.getCommentContent() == null) {
				notification.setCommentContent(COMMENT_NOT_EXIST_EXPRESSION);
			}
		}
		return notifications;
	}

	public void readList(String appId, String ids) {
		List<String> idList = Arrays.stream(ids.split(",")).map(String::trim).collect(Collectors.toList());
		Criteria filterCriteria = Criteria
				.where("notificationId").in(idList)
				.and("toUserId").is(appId);
		Query filter = Query.query(filterCriteria);
		Update updateOperation = new Update();
		updateOperation.set("read", true);
		mongoTemplate.updateMulti(filter, updateOperation, CommentsNotification.class);
	}

	public void clearAll(String appId, String ids) {
		List<String> idList = Arrays.stream(ids.split(",")).map(String::trim).collect(Collectors.toList());
		Criteria filterCriteria = Criteria
				.where("notificationId").in(idList)
				.and("toUserId").is(appId);
		Query filter = Query.query(filterCriteria);
		mongoTemplate.remove(filter, Notification.class, "notifications");
	}

}
