package rongxchen.socialmedia.service;

import java.util.Map;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import com.alibaba.fastjson2.JSONObject;
import rongxchen.socialmedia.models.entity.User;
import rongxchen.socialmedia.repository.UserRepository;
import rongxchen.socialmedia.utils.JwtUtil;
import rongxchen.socialmedia.utils.ObjectUtil;
import rongxchen.socialmedia.utils.WebsocketManager;

@Component
@Slf4j
public class ChatWebSocketHandler implements WebSocketHandler {

    @Resource
    private ObjectUtil objectUtil;

    @Resource
    private WebsocketManager websocketManager;

    @Resource
    private UserRepository userRepository;

    @Override // onopen
    public void afterConnectionEstablished(WebSocketSession session) {
        
    }

    @Override // onmessage
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        try {
            String payload = (String) message.getPayload();
            JSONObject jsonObject = objectUtil.read(payload, JSONObject.class);
            String mode = jsonObject.getString("mode");
            // init
            if ("init".equals(mode)) {
                String token = jsonObject.getString("token");
                Map<String, String> map = JwtUtil.decodeToken(token);
                String appId = map.get("appId");
                User user = userRepository.getByAppId(appId);
                // add to session map
                if (user != null) {
                    websocketManager.add(appId, session);
                } else {
                    session.sendMessage(new TextMessage("token expired"));
                    session.close();
                }
            }
        } catch (Exception e) {
            log.warn(e.getLocalizedMessage());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        websocketManager.remove(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

}
