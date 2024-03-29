package rongxchen.socialmedia.config;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rongxchen.socialmedia.service.azure.AzureMailService;

/**
 * @author CHEN Rongxin
 */
@Configuration
public class AzureMailConfig {

	@Value("${azure.communication.connection-string}")
	private String connectionString;

	@Value("${azure.communication.email.sender-address}")
	private String senderAddress;

	@Value("${azure.communication.email.sender-name}")
	private String senderName;

	@Value("${frontend.url}")
	private String frontendUrl;

	@Bean
	public AzureMailService azureMail() {
		AzureMailService azureMail = new AzureMailService();
		azureMail.setConnectionString(connectionString);
		azureMail.setSenderAddress(senderAddress);
		azureMail.setSenderName(senderName);
		azureMail.setFrontendUrl(frontendUrl);
		EmailClient emailClient = new EmailClientBuilder()
				.connectionString(connectionString)
				.buildClient();
		azureMail.setEmailClient(emailClient);
		return azureMail;
	}

}
