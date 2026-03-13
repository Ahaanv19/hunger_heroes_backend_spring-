package com.open.spring.mvc.mortevision.archive.stream;
// package com.open.spring.mvc.mortevision.stream;

// import com.corundumstudio.socketio.AckRequest;
// import com.corundumstudio.socketio.SocketIOClient;
// import com.corundumstudio.socketio.SocketIOServer;
// import com.corundumstudio.socketio.listener.DataListener;
// import org.springframework.stereotype.Component;

// import lombok.extern.slf4j.Slf4j;

// @Component
// @Slf4j
// public class SocketIOController {

//     protected final SocketIOServer socketServer;

//     public SocketIOController(SocketIOServer socketServer) {
//         this.socketServer = socketServer;
//         this.socketServer.addEventListener("demoEvent", SocketDetail.class, demoEvent);
//     }

//     public DataListener<SocketDetail> demoEvent = new DataListener<>() {
//         @Override
//         public void onData(SocketIOClient client, SocketDetail socketDetail, AckRequest ackRequest) {
//             log.info("Demo event received: {}", socketDetail);
//             // Add your business logic here.
//             ackRequest.sendAckData("Demo event received");
//         }
//     };
// }