package com.open.spring.mvc.mortevision.nativesocket;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class WebSocketHandler extends TextWebSocketHandler {

    private static Set<WebSocketSession> sessions = new HashSet<>();
    public String broadcasterID;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
    }

    @Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        if(session.getId().equals(broadcasterID))
        {
            broadcasterID=null;
        }
	}

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        JSONObject messageAsJson = new JSONObject(message.getPayload());
        String messageContext = messageAsJson.getString("context");
        String sdp;
        String returnID;
        String candidate;
        JSONObject payload = new JSONObject();
        switch (messageContext) {
            case "broadcastRequest":
                broadcasterID = session.getId();
                payload.put("context","broadcastRequestServer");

                for (WebSocketSession webSocketSession : sessions) {
                    if (webSocketSession.isOpen() && !webSocketSession.getId().equals(broadcasterID)) {
                        try {
                            webSocketSession.sendMessage(new TextMessage(payload.toString()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            break;

            case "stopBroadcast":
                if(session.getId()!=broadcasterID)
                {
                    return;
                }
                broadcasterID = null;
            break;

            case "viewerOfferClient":
                if (broadcasterID == null) {
                    return;
                }
                sdp = messageAsJson.getString("sdp");
                returnID = session.getId();
                payload.put("sdp", sdp);
                payload.put("returnID", returnID);
                payload.put("context","viewerOfferServer");

                for (WebSocketSession webSocketSession : sessions) {
                    if (webSocketSession.isOpen() && webSocketSession.getId().equals(broadcasterID)) {
                        try {
                            webSocketSession.sendMessage(new TextMessage(payload.toString()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
                break;

            case "viewerAcceptClient":
            if (broadcasterID == null) {
                return;
            }
                sdp = messageAsJson.getString("sdp");
                returnID = messageAsJson.getString("returnID");
                if (returnID == null) {
                    return;
                }

                payload = new JSONObject();
                payload.put("sdp", sdp);
                payload.put("context","viewerAcceptServer");

                for (WebSocketSession webSocketSession : sessions) {
                    if (webSocketSession.isOpen() && !session.getId().equals(returnID)) {
                        try {
                            webSocketSession.sendMessage(new TextMessage(payload.toString()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;

            case "iceToStreamerClient":
                if (broadcasterID == null) {
                    return;
                }
                candidate = messageAsJson.getString("candidate");
                payload.put("candidate", candidate);
                payload.put("context","iceToStreamerServer");

                for (WebSocketSession webSocketSession : sessions) {
                    if (webSocketSession.isOpen() && webSocketSession.getId().equals(broadcasterID)) {
                        try {
                            webSocketSession.sendMessage(new TextMessage(payload.toString()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            break;

            case "iceToViewerClient":
            if (broadcasterID == null) {
                return;
            }
                candidate = messageAsJson.getString("candidate");
                payload.put("candidate", candidate);
                payload.put("context","iceToViewerServer");

                for (WebSocketSession webSocketSession : sessions) {
                    if (webSocketSession.isOpen() && !webSocketSession.getId().equals(broadcasterID)) {
                        try {
                            webSocketSession.sendMessage(new TextMessage(payload.toString()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            break;

        }
    }
}