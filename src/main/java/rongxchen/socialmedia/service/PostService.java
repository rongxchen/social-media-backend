package rongxchen.socialmedia.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rongxchen.socialmedia.common.enums.MessageType;
import rongxchen.socialmedia.common.message_queue.RocketMQProducer;
import rongxchen.socialmedia.exceptions.HttpException;
import rongxchen.socialmedia.models.dto.PostDTO;
import rongxchen.socialmedia.models.entity.Post;
import rongxchen.socialmedia.models.mq.MessageMeta;
import rongxchen.socialmedia.models.vo.PostVO;
import rongxchen.socialmedia.repository.PostRepository;
import rongxchen.socialmedia.service.common.MyMongoService;
import rongxchen.socialmedia.utils.ObjectUtil;
import rongxchen.socialmedia.utils.UUIDGenerator;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
	private MyMongoService myMongoService;

	@Resource
	private RocketMQProducer rocketMQProducer;

	private final String BLOB_URL_PREFIX = "https://mysocialmediastorage.blob.core.windows.net/media";

	public String publishPost(MultipartFile[] files, String postJsonString, String appId) {
		PostDTO postDTO = objectUtil.readObject(postJsonString, PostDTO.class);
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
		List<MessageMeta> messageMetaList = new ArrayList<>();
		// send message to azure blob service to upload media files
		for (MultipartFile file : files) {
			String suffix = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf("."));
			String blobName = postId + "/" + UUIDGenerator.generate(true) + suffix;
			String imageUrl = BLOB_URL_PREFIX + "/" + blobName;
			imageList.add(imageUrl);
			MessageMeta messageMeta = new MessageMeta(MessageType.BLOB_POST_IMG.getValue());
			messageMeta.addString("blobName", blobName);
			messageMeta.addString("contentType", file.getContentType());
			try {
				messageMeta.addBytes("fileBytes", file.getBytes());
			} catch (IOException e) {
				throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to read file bytes");
			}
			messageMetaList.add(messageMeta);
		}
		rocketMQProducer.sendMessage("post-media-upload", messageMetaList);
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
						"likeCount", "favoriteCount", "commentCount", "createTime", "updateTime")
				.byPage(page, defaultSize)
				.fetchResult("posts", PostVO.class);
	}

	public void deletePost(String postId, String appId) {
		Post post = postRepository.getByPostId(postId);
		if (!post.getAuthorId().equals(appId)) {
			throw new HttpException(HttpStatus.FORBIDDEN, "not  authorized to delete this post");
		}
		List<String> blobNames = post.getImageList()
				.stream().map(x -> x.replace(BLOB_URL_PREFIX + "/", ""))
				.collect(Collectors.toList());
		MessageMeta messageMeta = new MessageMeta(MessageType.BLOB_POST_IMG.getValue());
		messageMeta.add("imageList", blobNames);
		rocketMQProducer.sendMessage("post-media-delete", messageMeta);
		postRepository.delete(post);
	}

}
