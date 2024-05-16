package rongxchen.socialmedia.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import com.alibaba.fastjson2.JSONObject;

import rongxchen.socialmedia.models.entity.User;
import rongxchen.socialmedia.repository.UserRepository;
import rongxchen.socialmedia.utils.ObjectUtil;

@Component
public class ChatWebSocketHandler implements WebSocketHandler {

    @Resource
    private ObjectUtil objectUtil;

    @Resource
    private UserRepository userRepository;

    private final ConcurrentHashMap<String, List<WebSocketSession>> chatSessionMap = new ConcurrentHashMap<>();

    @Override // onopen
    public void afterConnectionEstablished(WebSocketSession session) {
        
    }

    @Override // onmessage
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws IOException, InterruptedException {
        String payload = (String) message.getPayload();
        JSONObject jsonObject = objectUtil.read(payload, JSONObject.class);
        String mode = jsonObject.getString("mode");
        if ("init".equals(mode)) {
            String appId = jsonObject.getString("appId");
            User user = userRepository.getByAppId(appId);
            if (user != null) {
                // add to session map
                if (chatSessionMap.containsKey(appId)) {
                    chatSessionMap.get(appId).add(session);
                } else {
                    List<WebSocketSession> sessions = new ArrayList<>();
                    sessions.add(session);
                    chatSessionMap.put(appId, sessions);
                }
                System.out.println("session added, id: " + session.getId());
            } else {
                // if no such user, close the session directly
                session.close();
            }
            while (true) {
                for (Map.Entry<String, List<WebSocketSession>> entry : chatSessionMap.entrySet()) {
                    for (WebSocketSession _session : entry.getValue()) {
                        double price = Math.ceil(Math.random() * 100) * 100 / 100;
                        TextMessage textMessage = new TextMessage("price: " + price);
                        _session.sendMessage(textMessage);
                        System.out.println("sent: " + textMessage);
                    }
                }
                Thread.sleep(1000);
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        List<WebSocketSession> collect = null;
        String appId = null;
        for (Map.Entry<String, List<WebSocketSession>> entry : chatSessionMap.entrySet()) {
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
            chatSessionMap.put(appId, collect);
        }
        session.close();
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

}
