package rongxchen.socialmedia.config;

import lombok.Data;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author CHEN Rongxin
 */
@Configuration
@ConfigurationProperties(prefix = "rocketmq.producer")
@Data
public class RocketMQConfig {

	@Value("${rocketmq.name-server}")
	private String nameServer;

	private String group;

	private int sendMessageTimeout;

	private int retryTimesWhenSendFailed;

	private int retryTimesWhenSendAsyncFailed;

	@Bean
	public DefaultMQProducer defaultMQProducer() {
		DefaultMQProducer producer = new DefaultMQProducer();
		producer.setNamesrvAddr(nameServer);
		producer.setProducerGroup(group);
		producer.setSendMsgTimeout(sendMessageTimeout);
		producer.setRetryTimesWhenSendFailed(retryTimesWhenSendFailed);
		producer.setRetryTimesWhenSendAsyncFailed(retryTimesWhenSendAsyncFailed);
		return producer;
	}

	@Bean
	public RocketMQTemplate rocketMQTemplate(DefaultMQProducer defaultMQProducer) {
		RocketMQTemplate rocketMQTemplate = new RocketMQTemplate();
		rocketMQTemplate.setProducer(defaultMQProducer);
		return rocketMQTemplate;
	}

}
