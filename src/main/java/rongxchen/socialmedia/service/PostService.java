package rongxchen.socialmedia.service;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rongxchen.socialmedia.exceptions.HttpException;
import rongxchen.socialmedia.models.dto.PostDTO;
import rongxchen.socialmedia.models.entity.CollectItem;
import rongxchen.socialmedia.models.entity.Post;
import rongxchen.socialmedia.models.vo.PostVO;
import rongxchen.socialmedia.repository.CollectItemRepository;
import rongxchen.socialmedia.repository.PostRepository;
import rongxchen.socialmedia.service.azure.AzureBlobService;
import rongxchen.socialmedia.service.common.MyMongoService;
import rongxchen.socialmedia.utils.ObjectUtil;
import rongxchen.socialmedia.utils.UUIDGenerator;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author CHEN Rongxin
 */
@Service
public class PostService {

	@Resource
	private ObjectUtil objectUtil;

	@Resource
	private PostRepository postRepository;

	@Resource
	private CollectItemRepository collectItemRepository;

	@Resource
	private MyMongoService myMongoService;

	// TODO
//	@Resource
//	private RocketMQProducer rocketMQProducer;

	@Resource
	private AzureBlobService azureBlobService;

	private final String BLOB_URL_PREFIX = "https://mysocialmediastorage.blob.core.windows.net/media";

	public String publishPost(MultipartFile[] files, String postJsonString, String appId) {
		PostDTO postDTO = objectUtil.read(postJsonString, PostDTO.class);
		String postId = UUIDGenerator.generate("post");
		while (postRepository.getByPostId(postId) != null) {
			postId = UUIDGenerator.generate("post");
		}
		// set post object
		Post post = new Post();
		post.setPostId(postId);
		post.setTitle(postDTO.getTitle());
		post.setContent(postDTO.getContent());
		post.setAuthorId(appId);
		post.setLikeCount(0);
		post.setFavoriteCount(0);
		post.setCommentCount(0);
		post.setTagList(postDTO.getTags());
		post.setCreateTime(LocalDate.now());
		post.setLastModifiedTime(LocalDate.now());
		List<String> imageList = new ArrayList<>();
//		List<MQBody> mqBodyList = new ArrayList<>();
		// send message to azure blob service to upload media files
		for (MultipartFile file : files) {
			String suffix = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf("."));
			String blobName = postId + "/" + UUIDGenerator.generate(true) + suffix;
			String imageUrl = BLOB_URL_PREFIX + "/" + blobName;
			imageList.add(imageUrl);
//			MQBody mqBody = new MQBody("blob_post_img");
//			mqBody.add("blobName", blobName);
//			mqBody.add("contentType", file.getContentType());
			try {
//				mqBody.addBytes("fileBytes", file.getBytes());
				azureBlobService.uploadFile("media", blobName, file.getInputStream(), file.getContentType());
			} catch (IOException e) {
				throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to read file bytes");
			}
//			mqBodyList.add(mqBody);
		}
		// TODO
//		rocketMQProducer.sendMessage("post-media-upload", mqBodyList);
		post.setImageList(imageList);
		postRepository.save(post);
		return postId;
	}

	public List<PostVO> getPostByPage(long page) {
		int defaultSize = 20;
		return myMongoService
				.lookup("users", "appId", "authorId", "userInfo")
				.unwind("userInfo")
				.project("postId", "title", "content", "imageList", "tagList", "authorId",
						"userInfo.username as authorName", "userInfo.avatar as authorAvatar",
						"likeCount", "favoriteCount", "commentCount", "createTime", "lastModifiedTime")
				.byPage(page, defaultSize)
				.fetchResult("posts", PostVO.class);
	}

	public PostVO getPostByPostId(String postId) {
		return myMongoService
				.lookup("users", "appId", "authorId", "userInfo")
				.unwind("userInfo")
				.match(Criteria.where("postId").is(postId))
				.project("postId", "title", "content", "imageList", "tagList", "authorId",
						"userInfo.username as authorName", "userInfo.avatar as authorAvatar",
						"likeCount", "favoriteCount", "commentCount", "createTime", "lastModifiedTime")
				.fetchOne("posts", PostVO.class);
	}

	public boolean collectPost(String postId, String action, String collectType, String userId) {
		Post post = postRepository.getByPostId(postId);
		if (post == null) {
			throw new IllegalArgumentException("post not exist");
		}
		if ("collect".equals(action)) {
			switch (collectType) {
				case "likes": {
					post.setLikeCount(post.getLikeCount()+1);
					break;
				}
				case "favorites": {
					post.setFavoriteCount(post.getFavoriteCount()+1);
				}
			}
			postRepository.save(post);
			CollectItem item = new CollectItem();
			item.setItemId(postId);
			item.setUserId(userId);
			item.setItemType(collectType);
			item.setItemMeta("post");
			collectItemRepository.save(item);
		} else if ("cancel".equals(action)) {
			switch (collectType) {
				case "likes": {
					post.setLikeCount(post.getLikeCount()-1);
					break;
				}
				case "favorites": {
					post.setFavoriteCount(post.getFavoriteCount()-1);
				}
			}
			postRepository.save(post);
			CollectItem item = collectItemRepository.getByItemId(postId, collectType, userId);
			collectItemRepository.delete(item);
		} else {
			return false;
		}
		return true;
	}

	public Map<String, Map<String, Integer>> getCollectionRecord(String userId) {
		List<CollectItem> itemList = collectItemRepository.getByUserId(userId);
		Map<String, Map<String, Integer>> record = new HashMap<>();
		record.put("comments", new HashMap<>());
		record.put("likes", new HashMap<>());
		record.put("favorites", new HashMap<>());
		for (CollectItem item : itemList) {
			record.get(item.getItemType()).put(item.getItemId(), 1);
		}
		return record;
	}

	public void deletePost(String postId, String appId) {
		Post post = postRepository.getByPostId(postId);
		if (!post.getAuthorId().equals(appId)) {
			throw new HttpException(HttpStatus.FORBIDDEN, "not  authorized to delete this post");
		}
		List<String> blobNames = post.getImageList()
				.stream().map(x -> x.replace(BLOB_URL_PREFIX + "/", ""))
				.collect(Collectors.toList());
		// TODO
//		MQBody mqBody = new MQBody("blob_post_img");
//		mqBody.add("imageList", blobNames);
//		rocketMQProducer.sendMessage("post-media-delete", mqBody);

		for (String blobName : blobNames) {
			azureBlobService.removeFile("media", blobName);
		}
		postRepository.delete(post);
	}

}
