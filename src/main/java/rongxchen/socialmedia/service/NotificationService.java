package rongxchen.socialmedia.service;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import rongxchen.socialmedia.enums.NotificationCategory;
import rongxchen.socialmedia.models.entity.Comment;
import rongxchen.socialmedia.models.entity.Friend;
import rongxchen.socialmedia.models.entity.Post;
import rongxchen.socialmedia.models.entity.notifications.CommentsNotification;
import rongxchen.socialmedia.models.entity.notifications.FollowsNotification;
import rongxchen.socialmedia.models.entity.notifications.LikesNotification;
import rongxchen.socialmedia.models.entity.notifications.Notification;
import rongxchen.socialmedia.models.vo.notifications.CommentsNotificationVO;
import rongxchen.socialmedia.models.vo.notifications.FollowsNotificationVO;
import rongxchen.socialmedia.models.vo.notifications.LikesNotificationVO;
import rongxchen.socialmedia.repository.notifications.CommentsNotificationRepository;
import rongxchen.socialmedia.repository.notifications.FollowsNotificationRepository;
import rongxchen.socialmedia.repository.notifications.LikesNotificationRepository;
import rongxchen.socialmedia.service.common.MongoAggregation;
import rongxchen.socialmedia.service.common.MongoAggregationBuilder;
import rongxchen.socialmedia.service.common.MyMongoService;
import rongxchen.socialmedia.utils.DateUtil;
import rongxchen.socialmedia.utils.ObjectUtil;
import rongxchen.socialmedia.utils.UUIDGenerator;
import rongxchen.socialmedia.utils.WebsocketManager;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
		if (commentsNotification != null) {
			WebsocketManager.sendTo(comment.getReplyCommentUserId(), objectUtil.write(commentsNotification));
		}
	}

	public CommentsNotificationVO getCommentsNotification(String notificationId) {
		MongoAggregation aggregation = MongoAggregationBuilder.newBuilder()
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
				.build();
		CommentsNotificationVO notification = myMongoService.fetchOne(aggregation, "notifications", CommentsNotificationVO.class);
		if (notification != null) {
			notification.setDateTime(DateUtil.convertToDisplayTime(notification.getDateTime()));
			if (notification.getCommentContent() == null) {
				notification.setCommentContent(COMMENT_NOT_EXIST_EXPRESSION);
			}
		}
		return notification;
	}

	public List<CommentsNotificationVO> getCommentsNotificationList(String appId, long skip, long limit) {
		MongoAggregation aggregation = MongoAggregationBuilder.newBuilder()
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
						"postId", "fromPost.title as postTitle", "parentId", "fromComment.content as commentContent")
				.sort("dateTime", -1)
				.skip(skip)
				.limit(limit);

		List<CommentsNotificationVO> notifications = myMongoService.fetchResult(aggregation, "notifications", CommentsNotificationVO.class);
		for (CommentsNotificationVO notification : notifications) {
			notification.setDateTime(DateUtil.convertToDisplayTime(notification.getDateTime()));
			if (notification.getCommentContent() == null) {
				notification.setCommentContent(COMMENT_NOT_EXIST_EXPRESSION);
			}
		}
		return notifications;
	}

	public void sendFollowsNotification(Friend friend) {
		FollowsNotification notification = new FollowsNotification();
		notification.setNotificationId(generateUniqueId(NotificationCategory.FOLLOWS));
		notification.setFromUserId(friend.getFollowedByUserId());
		notification.setToUserId(friend.getFriendId());
		notification.setAppId(friend.getFollowedByUserId());
		notification.setDateTime(LocalDateTime.now());
		followsNotificationRepository.save(notification);
		FollowsNotificationVO notificationVO = getFollowsNotification(notification.getNotificationId());
		if (notificationVO != null) {
			WebsocketManager.sendTo(friend.getFriendId(), objectUtil.write(notificationVO));
		}
	}

	public FollowsNotificationVO getFollowsNotification(String notificationId) {
		MongoAggregation aggregation = MongoAggregationBuilder.newBuilder()
				.match(Criteria.where("notificationId").is(notificationId)
						.and("notificationCategory").is(NotificationCategory.FOLLOWS.getCategory()))
				.lookup("users", "appId", "appId", "fromUser")
				.unwind("fromUser", true)
				.project("notificationId", "fromUserId", "toUserId", "dateTime", "read",
						"fromUser.username as fromUsername", "fromUser.avatar as fromUserAvatar", "appId");
		FollowsNotificationVO notification = myMongoService.fetchOne(aggregation, "notifications", FollowsNotificationVO.class);
		if (notification != null) {
			notification.setDateTime(DateUtil.convertToDisplayTime(notification.getDateTime()));
			if (notification.getAppId() == null) {
				notification.setFromUsername("account deactivated");
			}
		}
		return notification;
	}

	public List<FollowsNotificationVO> getFollowsNotificationList(String appId, long skip, long limit) {
		MongoAggregation aggregation = MongoAggregationBuilder.newBuilder()
				.match(Criteria.where("toUserId").is(appId)
						.and("notificationCategory").is(NotificationCategory.FOLLOWS.getCategory()))
				.lookup("users", "appId", "appId", "fromUser")
				.unwind("fromUser", true)
				.project("notificationId", "fromUserId", "toUserId", "dateTime", "read",
						"fromUser.username as fromUsername", "fromUser.avatar as fromUserAvatar", "appId")
				.sort("dateTime", -1)
				.skip(skip)
				.limit(limit);
		List<FollowsNotificationVO> notificationList = myMongoService.fetchResult(aggregation, "notifications", FollowsNotificationVO.class);
		for (FollowsNotificationVO notification : notificationList) {
			notification.setDateTime(DateUtil.convertToDisplayTime(notification.getDateTime()));
			if (notification.getAppId() == null) {
				notification.setFromUsername("account deactivated");
			}
		}
		return notificationList;
	}

	public void sendLikePostNotification(Post post, String action, String fromUserId) {
		LikesNotification notification = new LikesNotification();
		notification.setNotificationId(generateUniqueId(NotificationCategory.LIKES));
		notification.setFromUserId(fromUserId);
		notification.setToUserId(post.getAuthorId());
		notification.setPostId(post.getPostId());
		notification.setItemId(post.getPostId());
		notification.setItemType("post");
		notification.setAction(action);
		notification.setDateTime(LocalDateTime.now());
		likesNotificationRepository.save(notification);
		LikesNotificationVO likesNotification = getLikesNotification(notification.getNotificationId());
		if (likesNotification != null) {
			WebsocketManager.sendTo(post.getAuthorId(), objectUtil.write(likesNotification));
		}
	}

	public void sendLikeCommentNotification(Comment comment, String fromUserId) {
		LikesNotification notification = new LikesNotification();
		notification.setNotificationId(generateUniqueId(NotificationCategory.LIKES));
		notification.setFromUserId(fromUserId);
		notification.setToUserId(comment.getAuthorId());
		notification.setPostId(comment.getPostId());
		notification.setItemId(comment.getCommentId());
		notification.setItemType("comment");
		notification.setAction("likes");
		notification.setDateTime(LocalDateTime.now());
		likesNotificationRepository.save(notification);
		LikesNotificationVO likesNotification = getLikesNotification(notification.getNotificationId());
		if (likesNotification != null) {
			WebsocketManager.sendTo(comment.getCommentId(), objectUtil.write(likesNotification));
		}
	}

	public LikesNotificationVO getLikesNotification(String notificationId) {
		MongoAggregation aggregation = MongoAggregationBuilder.newBuilder()
				.match(Criteria.where("notificationId").is(notificationId)
						.and("notificationCategory").is(NotificationCategory.LIKES.getCategory()))
				.lookup("users", "appId", "fromUserId", "fromUser")
				.lookup("posts", "postId", "postId", "fromPost")
				.lookup("posts", "postId", "itemId", "itemPost")
				.lookup("comments", "commentId", "itemId", "itemComment")
				.unwind("fromUser", true)
				.unwind("fromPost", true)
				.unwind("itemPost", true)
				.unwind("itemComment", true)
				.project("notificationId", "fromUserId", "toUserId", "dateTime", "read",
						"fromUser.username as fromUsername", "fromUser.avatar as fromUserAvatar",
						"fromPost.postId as postId", "action", "itemType", "itemId")
				.conditionalIfNull("itemPost", "title", "itemComment", "content", "content");
		LikesNotificationVO notification = myMongoService.fetchOne(aggregation, "notifications", LikesNotificationVO.class);
		if (notification != null) {
			notification.setDateTime(DateUtil.convertToDisplayTime(notification.getDateTime()));
			if (notification.getContent() == null) {
				notification.setContent("content has been deleted");
			}
			if (notification.getFromUsername() == null) {
				notification.setFromUsername("account deactivated");
			}
		}
		return notification;
	}

	public List<LikesNotificationVO> getLikesNotificationList(String appId, long skip, long limit) {
		MongoAggregation aggregation = MongoAggregationBuilder.newBuilder()
				.match(Criteria.where("toUserId").is(appId)
						.and("notificationCategory").is(NotificationCategory.LIKES.getCategory()))
				.lookup("users", "appId", "fromUserId", "fromUser")
				.lookup("posts", "postId", "postId", "fromPost")
				.lookup("posts", "postId", "itemId", "itemPost")
				.lookup("comments", "commentId", "itemId", "itemComment")
				.unwind("fromUser", true)
				.unwind("fromPost", true)
				.unwind("itemPost", true)
				.unwind("itemComment", true)
				.project("notificationId", "fromUserId", "toUserId", "dateTime", "read",
						"fromUser.username as fromUsername", "fromUser.avatar as fromUserAvatar",
						"fromPost.postId as postId", "action", "itemType", "itemId")
				.conditionalIfNull("itemPost", "title", "itemComment", "content", "content")
				.sort("dateTime", -1)
				.skip(skip)
				.limit(limit);
		List<LikesNotificationVO> notifications = myMongoService.fetchResult(aggregation, "notifications", LikesNotificationVO.class);
		// handle missing content for comments
		Map<String, String> missingCommentContent = findMissingCommentContent(notifications);
		for (LikesNotificationVO notification : notifications) {
			notification.setDateTime(DateUtil.convertToDisplayTime(notification.getDateTime()));
			if (notification.getContent() == null) {
				notification.setContent(missingCommentContent.getOrDefault(notification.getItemId(),
						"content has been deleted"));
			}
			if (notification.getFromUsername() == null) {
				notification.setFromUsername("account deactivated");
			}
		}
		return notifications;
	}

	private Map<String, String> findMissingCommentContent(List<LikesNotificationVO> notificationList) {
		List<String> idList = notificationList.stream()
				.filter(x -> x.getContent() == null)
				.map(LikesNotificationVO::getItemId).collect(Collectors.toList());
		MongoAggregation aggregation = MongoAggregationBuilder.newBuilder()
				.match(Criteria.where("commentId").in(idList))
				.project("commentId", "content");
		List<Document> documents = myMongoService.fetchResult(aggregation, "comments", Document.class);
		return documents.stream().collect(Collectors.toMap(
				x -> x.getString("commentId"),
				x -> x.getString("content")));
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
