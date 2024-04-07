package rongxchen.socialmedia;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import rongxchen.socialmedia.common.message_queue.RocketMQProducer;
import rongxchen.socialmedia.models.dto.PostDTO;
import rongxchen.socialmedia.models.entity.Post;
import rongxchen.socialmedia.models.mq.MessageMeta;
import rongxchen.socialmedia.models.vo.PostVO;
import rongxchen.socialmedia.service.common.MyMongoService;
import rongxchen.socialmedia.utils.ObjectUtil;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author CHEN Rongxin
 */
@SpringBootTest
public class GeneralTest {

	@Resource
	ObjectUtil objectUtil;

	@Test
	void test() {
		List<MessageMeta> messageMetaList = new ArrayList<>();
		MessageMeta messageMeta = new MessageMeta("hello");
		messageMeta.addString("test", "hello");
		messageMeta.addBytes("bytes", new byte[]{1, 2, 3});
		messageMetaList.add(messageMeta);
		messageMetaList.add(messageMeta);
		String jsonString = objectUtil.writeObjectListAsString(messageMetaList);
		List<MessageMeta> jsonList = objectUtil.readObjectList(jsonString, MessageMeta.class);
	}

}
