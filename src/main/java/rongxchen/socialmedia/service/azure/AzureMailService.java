package rongxchen.socialmedia.service.azure;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.models.EmailMessage;
import lombok.Data;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.util.FileCopyUtils;
import rongxchen.socialmedia.common.enums.UserRole;
import rongxchen.socialmedia.exceptions.HttpException;
import rongxchen.socialmedia.utils.JwtUtil;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author CHEN Rongxin
 */
@Data
public class AzureMailService {

	private String connectionString;

	private String senderAddress;

	private String senderName;

	private String frontendUrl;

	private EmailClient emailClient;

	@Resource
	private ResourceLoader resourceLoader;

	public AzureMailService() {

	}

	private EmailMessage buildMessage(String subject, String bodyHtml, String...recipientAddress) {
		return new EmailMessage()
				.setSenderAddress(senderAddress)
				.setToRecipients(recipientAddress)
				.setSubject(subject)
				.setBodyHtml(bodyHtml);
	}

	public void sendEmail(String subject, String bodyHtml, String...address) {
		EmailMessage message = buildMessage(subject, bodyHtml, address);
		emailClient.beginSend(message);
	}

	public void sendVerificationCode(String address, String code) {
		String subject = "Verification Code";
		String bodyHtml = readHTMLFromFile("mails/send_verification_code.html");
		bodyHtml = String.format(bodyHtml, code, senderName);
		sendEmail(subject, bodyHtml, address);
	}

	public void sendResetPassword(String appId, String username, String address) {
		String subject = "Reset Password";
		Map<String, String> claims = new HashMap<>();
		claims.put("appId", appId);
		claims.put("role", UserRole.USER.getRole());
		String token = JwtUtil.generateToken(claims, 30);
		String url = frontendUrl + "/reset-password?token=" + token;
		String bodyHtml = readHTMLFromFile("mails/reset_password.html");
		bodyHtml = String.format(bodyHtml, username, url, senderName);
		sendEmail(subject, bodyHtml, address);
	}

	public String readHTMLFromFile(String filePath) {
		try {
			org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:" + filePath);
			byte[] fileData = FileCopyUtils.copyToByteArray(resource.getInputStream());
			return new String(fileData, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "fail to read email templates");
		}
	}

}
