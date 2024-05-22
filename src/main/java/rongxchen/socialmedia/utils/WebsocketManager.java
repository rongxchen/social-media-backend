package rongxchen.socialmedia.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

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
public class WebsocketManager {

	public static final ConcurrentHashMap<String, List<WebSocketSession>> sessionMap = new ConcurrentHashMap<>();

	private WebsocketManager() {}

	public static void add(String appId, WebSocketSession session) {
		if (sessionMap.containsKey(appId)) {
			sessionMap.get(appId).add(session);
		} else {
			List<WebSocketSession> sessions = new ArrayList<>();
			sessions.add(session);
			sessionMap.put(appId, sessions);
		}
		log.info("session added, id: " + session.getId());
	}

	public static void remove(WebSocketSession session) {
		List<WebSocketSession> collect = null;
		String appId = null;
		for (Map.Entry<String, List<WebSocketSession>> entry : sessionMap.entrySet()) {
			for (WebSocketSession _session : entry.getValue()) {
				String _address = Objects.requireNonNull(_session.getRemoteAddress()).getAddress().getHostAddress();
				String address = Objects.requireNonNull(session.getRemoteAddress()).getAddress().getHostAddress();
				if (_address.equals(address)) {
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

	public static void sendTo(String appId, String msg) {
		if (!sessionMap.containsKey(appId)) {
			return;
		}
		for (WebSocketSession session : sessionMap.get(appId)) {
			if (session.isOpen()) {
				try {
					session.sendMessage(new TextMessage(msg));
				} catch (IOException e) {
					log.warn(e.getMessage());
				}
			}
		}
	}

	public static void broadcast(String msg) {
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
