package com.open.spring.mvc.mortevision.archive.socketio;
    // package com.open.spring.mvc.mortevision.stream;

    // import java.util.UUID;

    // import org.springframework.stereotype.Component;

    // import com.corundumstudio.socketio.SocketIOClient;
    // import com.corundumstudio.socketio.SocketIOServer;
    // import com.corundumstudio.socketio.listener.ConnectListener;
    // import com.corundumstudio.socketio.listener.DataListener;
    // import com.corundumstudio.socketio.listener.DisconnectListener;

    // @Component
    // public class SocketModule {


    //     private final SocketIOServer server;
    //     private final SocketService socketService;
    //     private SocketIOClient streamer;
    //     private UUID streamerid;

    //     public SocketModule(SocketIOServer server, SocketService socketService) {
    //         this.server = server;
    //         this.socketService = socketService;
    //         server.addConnectListener(onConnected());
    //         server.addDisconnectListener(onDisconnected());

    //         server.addEventListener("streamReady", SDPData.class, streamReady());

    //         server.addEventListener("viewOfferClient", SDPData.class,viewOfferClient());
    //         server.addEventListener("viewAcceptClient", SDPData.class, viewAcceptClient());

    //         server.addEventListener("sendIceToViewersClient", iceData.class, sendIceToViewers());
    //         server.addEventListener("sendIceToStreamerClient", iceData.class, sendIceToStreamer());


    //     }
        
    //     private DataListener<iceData> sendIceToStreamer() {
    //         return (senderClient, data, ackSender) -> {
    //             if(streamer==null)
    //             {
    //                 return;
    //             }
    //             iceData candidate = new iceData();
    //             candidate.setCandidate(data.getCandidate());
    //             // System.out.println(candidate);
    //             socketService.sendIceToStreamer("sendIceToStreamerServer", streamer, candidate);
    //         };
    //     }


    //     private DataListener<iceData> sendIceToViewers() {
    //         return (senderClient, data, ackSender) -> {
    //             if(streamer==null)
    //             {
    //                 return;
    //             }
    //             iceData candidate = new iceData();
    //             candidate.setCandidate(data.getCandidate());
    //             // System.out.println(candidate);
    //             socketService.sendIceToViewers(server, "sendIceToViewersServer", streamer, candidate);
    //         };
    //     }

    //     private DataListener<SDPData> streamReady() {
    //         return (senderClient, data, ackSender) -> {
    //             streamer = senderClient;
    //             streamerid = streamer.getSessionId();
    //             // System.out.println(streamer);
    //         };
    //     }

    //     private DataListener<SDPData> viewOfferClient() {
    //         return (senderClient, data, ackSender) -> {
    //             if(streamer==null)
    //             {
    //                 return;
    //             }
    //             System.out.println(streamer);
    //             System.out.println("streamer is " + streamer);
    //             // System.out.println(data.getSdp());
    //             // System.out.println("view offer client");
    //             SDPData toSend = new SDPData();
    //             toSend.setSdp(data.getSdp());
    //             toSend.setType(data.getType());
    //             toSend.setSenderUID(senderClient.getSessionId().toString());
    //             // System.out.println(toSend.getSenderUID());

    //             socketService.sendData("viewOfferServer", streamer, toSend);
    //         };
    //     }

    //     private DataListener<SDPData> viewAcceptClient() {
    //         return (senderClient, data, ackSender) -> {
    //             if(streamer==null)
    //             {
    //                 return;
    //             }
    //             SDPData toSend = new SDPData();
    //             toSend.setSdp(data.getSdp());
    //             toSend.setType(data.getType());
    //             // toSend.setSenderUID(senderClient.getSessionId().toString());
    //             // System.out.println(data.getSdp());
    //             // System.out.println(server.getClient(UUID.fromString(data.getSenderUID())));
    //             // System.out.println("view accept client");

    //             socketService.sendData("viewAcceptServer", 
    //             server.getClient(UUID.fromString(data.getSenderUID())), toSend);
    //         };
    //     }


    //     private ConnectListener onConnected() {
    //         return (client) -> {
    //             String room = "mortevision";
    //             client.joinRoom(room);
    //             System.out.println("Socket ID[{}]  Connected to socket"+ client.getSessionId().toString());
    //         };

    //     }

    //     private DisconnectListener onDisconnected() {
    //         return client -> {
    //             if(client.getSessionId().toString().equals(streamerid.toString()))
    //             {
    //                 streamer=null;
    //                 streamerid=null;
    //             }
    //             System.out.println("Client[{}] - Disconnected from socket"+  client.getSessionId().toString());
    //         };
    //     }

    // }