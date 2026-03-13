package com.open.spring.mvc.mortevision.archive.socketio;
// package com.open.spring.mvc.mortevision.stream;

// import org.springframework.stereotype.Service;

// import com.corundumstudio.socketio.SocketIOClient;
// import com.corundumstudio.socketio.SocketIOServer;

// @Service
// public class SocketService {

//     public void sendData(String eventName, SocketIOClient reciever, SDPData data) {

//         reciever.sendEvent(eventName,
//                 data);
//     }

//     public void sendIceToViewers(SocketIOServer server, String eventName, SocketIOClient sender, iceData data) {

//         for (SocketIOClient client : server.getAllClients()) {
//             if (!client.getSessionId().equals(sender.getSessionId())) {
//                 client.sendEvent(eventName,data);
//             }
//         }
//     }

//     public void sendIceToStreamer(String eventName, SocketIOClient reciever, iceData data) {

//         reciever.sendEvent(eventName, data);
//     }

// }