package com.open.spring.mvc.mortevision.archive.stream;
// package com.open.spring.mvc.mortevision.stream;

// import java.util.HashMap;
// import java.util.Map;

// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// @RestController
// @RequestMapping("/webrtc")
// public class StreamSignal {

//     private String streamerSDP;



//     @PostMapping("/consume")
//     public Map<String,String> consumer(@RequestBody String body) {
//         HashMap<String, String> map = new HashMap<>();
//         if(streamerSDP==null)
//         {
//             map.put("error","streamer's SDP is missing");
//             return map;
//         }
//         map.put("sdp",streamerSDP);
//         return map;
//     }

//     @PostMapping(value="/broadcast",consumes ="application/json")
//     public Map<String, String> broadcast(@RequestBody String body) {
//         String sdp = new JSONObject(body).getJSONObject("sdp").getString("sdp"); 
//         });

//         peerConnection.createAnswer(new RTCAnswerOptions(), new CreateSessionDescriptionObserver() {

//             @Override
//             public void onSuccess(RTCSessionDescription description) {
//                 peerConnection.setLocalDescription(description, new SetSessionDescriptionObserver() {

//                     @Override
//                     public void onSuccess() {
//                         // throw new UnsupportedOperationException("Unimplemented method 'onSuccess'");
//                     }

//                     @Override
//                     public void onFailure(String error) {
//                         // throw new UnsupportedOperationException("Unimplemented method 'onFailure'");
//                     }
                    
//                 });
//             }

//             @Override
//             public void onFailure(String error) {
//                 // throw new UnsupportedOperationException("Unimplemented method 'onFailure'");
//             }

//         });
//         HashMap<String, String> map = new HashMap<>();
//         map.put("type",peerConnection.getLocalDescription().sdpType.name().toLowerCase());
//         map.put("sdp",peerConnection.getLocalDescription().sdp);
//         return map;
//     }

//     private final Object lock = new Object();

//     @Override
//     public void onAddTrack(RTCRtpReceiver receiver, MediaStream[] mediaStreams) {
//         synchronized (lock) {
//         System.out.println("track add");
//         if(receiver.getTrack().getKind().equals("video"))
//         {
//             videoTrack = receiver.getTrack();
//         }
//         /* 
//         else if(receiver.getTrack().getKind().equals("audio"))
//         {
//             audioTrack = receiver.getTrack();
//         }
//             */
//     }
//     }

//     @Override
//     public void onIceCandidate(RTCIceCandidate candidate) {
//     }
// }
// }
