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
    String identifier = message['identifier'];
    String type = message['type'];
    Map data = message['data'];
    onMessageController.add(new Message(identifier, type, data));
  }

  void sendIceCandidate(String identifier, RtcIceCandidate iceCandidate) {
    send(encodedIceCandidate(identifier, iceCandidate));
  }

  void offer(String identifier, RtcSessionDescription offer) {
    send(encodeSessionDescription(identifier, 'media.publisher.webrtc.offer', offer));
  }

  void sendSubscriberIceCandidate(String identifier, RtcIceCandidate iceCandidate) {
    send(_encodedIceCandidate(identifier, 'media.subscriber.webrtc.ice-candidate', iceCandidate));
  }

  _encodedIceCandidate(String identifier, String type, RtcIceCandidate iceCandidate) {
    var message = {'identifier': identifier, 'type': type, 'data': {
        'candidate': iceCandidate.candidate,
        'sdpMid': iceCandidate.sdpMid,
        'sdpMLineIndex': iceCandidate.sdpMLineIndex
    }};

    return JSON.encode(message);
  }


  void subscriberAnswer(String identifier, RtcSessionDescription offer) {
    send(encodeSessionDescription(identifier, 'media.subscriber.webrtc.answer', offer));
  }

  encodedIceCandidate(String identifier, RtcIceCandidate iceCandidate) {
    var message = {'identifier': identifier, 'type': 'media.publisher.webrtc.ice-candidate', 'data': {
        'candidate': iceCandidate.candidate,
        'sdpMid': iceCandidate.sdpMid,
        'sdpMLineIndex': iceCandidate.sdpMLineIndex
    }};

    return JSON.encode(message);
  }

  encodeSessionDescription(String identifier, String type, RtcSessionDescription s) {
    var message = {'identifier': identifier, 'type': type, 'data': {'sdp': s.sdp, 'type': s.type}};
    return JSON.encode(message);
  }
}
