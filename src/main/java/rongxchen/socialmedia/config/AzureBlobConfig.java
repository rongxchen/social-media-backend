package rongxchen.socialmedia.config;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author CHEN Rongxin
 */
@Configuration
public class AzureBlobConfig {

	@Value("${spring.cloud.azure.storage.blob.account-name}")
	private String accountName;

	@Value("${spring.cloud.azure.storage.blob.account-key}")
	private String accountKey;

	@Value("${spring.cloud.azure.storage.blob.endpoint}")
	private String endPoint;

	@Bean
	public BlobServiceClient blobServiceClient() {
		return new BlobServiceClientBuilder()
				.endpoint(endPoint)
				.credential(new StorageSharedKeyCredential(accountName, accountKey))
				.buildClient();
	}

}
