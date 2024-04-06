package rongxchen.socialmedia.service.azure;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;

/**
 * @author CHEN Rongxin
 */
@Service
public class AzureBlobService {

	@Resource
	private BlobServiceClient blobServiceClient;

	public void uploadFile(String containerName, String blobName, InputStream inputStream, String contentType) {
		BlobClient blobClient = blobServiceClient.getBlobContainerClient(containerName)
				.getBlobClient(blobName);
		blobClient.upload(inputStream);
		BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(contentType);
		blobClient.setHttpHeaders(headers);
	}

	public void removeFile(String containerName, String blobName) {
		BlobClient blobClient = blobServiceClient.getBlobContainerClient(containerName)
				.getBlobClient(blobName);
		blobClient.deleteIfExists();
	}

}
