package rongxchen.socialmedia.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import rongxchen.socialmedia.models.vo.SocketMessageEntity;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author CHEN Rongxin
 */
@Slf4j
@Component
public class WebsocketManager {

	@Resource
	private ObjectUtil objectUtil;

	public static final ConcurrentHashMap<String, List<WebSocketSession>> sessionMap = new ConcurrentHashMap<>();

	private WebsocketManager() {}

	public void add(String appId, WebSocketSession session) {
		if (sessionMap.containsKey(appId)) {
			sessionMap.get(appId).add(session);
		} else {
			List<WebSocketSession> sessions = new ArrayList<>();
			sessions.add(session);
			sessionMap.put(appId, sessions);
		}
		log.info("session added, id: " + Objects.requireNonNull(session.getRemoteAddress()).getAddress().getHostAddress());
	}

	public void remove(WebSocketSession session) {
		List<WebSocketSession> collect = null;
		String appId = null;
		for (Map.Entry<String, List<WebSocketSession>> entry : sessionMap.entrySet()) {
			for (WebSocketSession _session : entry.getValue()) {
				String _address = Objects.requireNonNull(_session.getRemoteAddress()).getAddress().getHostAddress();
				String address = Objects.requireNonNull(session.getRemoteAddress()).getAddress().getHostAddress();
				if (_address.equals(address)) {
					log.warn(String.format("session %s removed", address));
					collect = entry.getValue().stream()
							.filter(x -> !Objects.requireNonNull(x.getRemoteAddress()).getAddress().getHostAddress().equals(session.getRemoteAddress().getAddress().getHostAddress()))
							.collect(Collectors.toList());
					appId = entry.getKey();
					break;
				}
			}
		}
		if (appId != null) {
			sessionMap.put(appId, collect);
		}
		try {
			session.close();
		} catch (IOException e) {
			log.warn(e.getMessage());
		}
	}

	public <T> void sendTo(String appId, SocketMessageEntity<T> messageEntity) {
		String msg = objectUtil.write(messageEntity);
		if (!sessionMap.containsKey(appId)) {
			return;
		}
		for (WebSocketSession session : sessionMap.get(appId)) {
			if (session.isOpen()) {
				try {
					session.sendMessage(new TextMessage(msg));
					log.info("sending to " + appId + ": " + msg);
				} catch (IOException e) {
					log.warn(e.getMessage());
				}
			}
		}
	}

	public <T> void broadcast(SocketMessageEntity<T> messageEntity) {
		String msg = objectUtil.write(messageEntity);
		for (Map.Entry<String, List<WebSocketSession>> entry : sessionMap.entrySet()) {
			for (WebSocketSession session : entry.getValue()) {
				if (session.isOpen()) {
					try {
						session.sendMessage(new TextMessage(msg));
					} catch (IOException e) {
						log.warn(e.getMessage());
					}
				}
			}
		}
	}

}
