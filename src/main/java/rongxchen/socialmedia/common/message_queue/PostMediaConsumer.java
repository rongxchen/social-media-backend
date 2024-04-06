package rongxchen.socialmedia.common.message_queue;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import rongxchen.socialmedia.models.mq.MessageMeta;
import rongxchen.socialmedia.service.azure.AzureBlobService;
import rongxchen.socialmedia.utils.ObjectUtil;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * @author CHEN Rongxin
 */
@Component
@RocketMQMessageListener(
		topic = "post-media-upload",
		consumerGroup = "${rocketmq.consumer.group}"
)
public class PostMediaConsumer implements RocketMQListener<String> {

	@Resource
	private AzureBlobService azureBlobService;

	@Resource
	private ObjectUtil objectUtil;

	@Override
	public void onMessage(String message) {
		List<MessageMeta> messageMetaList = objectUtil.readObjectList(message, MessageMeta.class);
		for (MessageMeta messageMeta : messageMetaList) {
			if ("blob_post_img".equals(messageMeta.getMessageType())) {
				String blobName = messageMeta.getString("blobName");
				System.out.println(blobName);
				String contentType = messageMeta.getString("contentType");
				InputStream fileRaw = new ByteArrayInputStream(messageMeta.getBytes("fileBytes"));
				azureBlobService.uploadFile("media", blobName, fileRaw, contentType);
				break;
			}
		}
	}

}

@Component
@RocketMQMessageListener(
		topic = "post-media-delete",
		consumerGroup = "${rocketmq.consumer.group}"
)
class PostMediaDeleteConsumer implements RocketMQListener<String> {

	@Resource
	private AzureBlobService azureBlobService;

	@Resource
	private ObjectUtil objectUtil;

	@Override
	public void onMessage(String message) {
		MessageMeta messageMeta = objectUtil.readObject(message, MessageMeta.class);
		if ("blob_post_img".equals(messageMeta.getMessageType())) {
			List<String> imageList = messageMeta.get("imageList");
			for (String image : imageList) {
				azureBlobService.removeFile("media", image);
			}
		}
	}

}
