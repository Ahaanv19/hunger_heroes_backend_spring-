package com.open.spring.mvc.mortevision.archive;

// import org.springframework.web.socket.CloseStatus;
// import org.springframework.web.socket.TextMessage;
// import org.springframework.web.socket.WebSocketSession;
// import org.springframework.web.socket.handler.TextWebSocketHandler;

// import java.util.ArrayList;
// import java.util.List;

public class SignalingHandler {}//extends TextWebSocketHandler {
//     private final List<WebSocketSession> sessions = new ArrayList<>();

//     @Override
//     public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//         sessions.add(session);
//     }

//     @Override
//     protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//         for (WebSocketSession peerSession : sessions) {
//             if (!peerSession.getId().equals(session.getId()) && peerSession.isOpen()) {
//                 peerSession.sendMessage(message); 
//             }
//         }
//     }

//     @Override
//     public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//         sessions.remove(session);
//     }
// }
