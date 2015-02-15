part of webrtc.signal;

abstract class SignalHandler {
  StreamController onMessageController = new StreamController.broadcast();
  Stream get onMessage => onMessageController.stream;

  var iceServers = {
      'iceServers': [{
          'url': 'stun:stun.l.google.com:19302'
      }]
  };

  SignalHandler();

  /// Abstract Methods
  void send(data);

  /// Concrete Methods
  void onSignal(Map message) {
    String type = message['type'];
    Map data = message['data'];
    onMessageController.add(new Message(type, data));
  }


  void sendIceCandidate(RtcIceCandidate iceCandidate) {
    send(encodedIceCandidate(iceCandidate));
  }

  void offer(RtcSessionDescription offer) {
    send(encodeSessionDescription('media.publisher.webrtc.offer', offer));
  }

  void sendSubscriberIceCandidate(RtcIceCandidate iceCandidate) {
    send(_encodedIceCandidate('media.subscriber.webrtc.ice-candidate', iceCandidate));
  }

  _encodedIceCandidate(String type, RtcIceCandidate iceCandidate) {
    var message = {'type': type, 'data': {
        'candidate': iceCandidate.candidate,
        'sdpMid': iceCandidate.sdpMid,
        'sdpMLineIndex': iceCandidate.sdpMLineIndex
    }};

    return JSON.encode(message);
  }



  void subscriberAnswer(RtcSessionDescription offer) {
    send(encodeSessionDescription('media.subscriber.webrtc.answer', offer));
  }

  encodedIceCandidate(RtcIceCandidate iceCandidate) {
    var message = {'type': 'media.publisher.webrtc.ice-candidate', 'data': {
        'candidate': iceCandidate.candidate,
        'sdpMid': iceCandidate.sdpMid,
        'sdpMLineIndex': iceCandidate.sdpMLineIndex
    }};

    return JSON.encode(message);
  }

  encodeSessionDescription(String type, RtcSessionDescription s) {
    var message = {'type': type, 'data': {'sdp': s.sdp, 'type': s.type}};
    return JSON.encode(message);
  }
}
