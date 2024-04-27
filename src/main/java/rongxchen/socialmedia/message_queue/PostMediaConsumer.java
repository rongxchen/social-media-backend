package rongxchen.socialmedia.message_queue;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import rongxchen.socialmedia.models.mq.MQBody;
import rongxchen.socialmedia.service.azure.AzureBlobService;
import rongxchen.socialmedia.utils.ObjectUtil;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * @author CHEN Rongxin
 */
//@Component
@RocketMQMessageListener(
		topic = "post-media-upload",
		consumerGroup = "${rocketmq.consumer.group}"
)
@Slf4j
public class PostMediaConsumer implements RocketMQListener<String> {

	@Resource
	private AzureBlobService azureBlobService;

	@Resource
	private ObjectUtil objectUtil;

	@Override
	public void onMessage(String message) {
		List<MQBody> mqBodyList = objectUtil.readList(message, MQBody.class);
		for (MQBody mqBody : mqBodyList) {
			if ("blob_post_img".equals(mqBody.getMessageType())) {
				String blobName = mqBody.get("blobName");
				log.info(blobName);
				String contentType = mqBody.get("contentType");
				InputStream fileRaw = new ByteArrayInputStream(mqBody.getBytes("fileBytes"));
				azureBlobService.uploadFile("media", blobName, fileRaw, contentType);
				break;
			}
		}
	}

}

//@Component
@RocketMQMessageListener(
		topic = "post-media-delete",
		consumerGroup = "${rocketmq.consumer.group}"
)
@Slf4j
class PostMediaDeleteConsumer implements RocketMQListener<String> {

	@Resource
	private AzureBlobService azureBlobService;

	@Resource
	private ObjectUtil objectUtil;

	@Override
	public void onMessage(String message) {
		MQBody mqBody = objectUtil.read(message, MQBody.class);
		log.info(mqBody.toString());
		if ("blob_post_img".equals(mqBody.getMessageType())) {
			List<String> imageList = mqBody.get("imageList");
			log.info(imageList.toString());
			for (String image : imageList) {
				azureBlobService.removeFile("media", image);
			}
		}
	}

}
