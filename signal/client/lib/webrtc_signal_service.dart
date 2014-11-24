library angular.webrtc.signal.service;

import 'dart:js';
import 'dart:async';
import 'dart:html';
import 'dart:convert' show JSON;
import 'package:logging/logging.dart';

import 'package:angular/angular.dart';

@Injectable()
class WebRtcSignalService {
  WebSocket webSocket;
  Function handleAnswer;
  Function handleOffer;
  Function handleIceCandidate;

  final Logger log = new Logger('WebRtcSignalService');

  WebRtcSignalService() {}

  Future initialize() {
    Completer completer = new Completer();

    var uri = 'ws://127.0.0.1:1234/ws';
    webSocket = new WebSocket(uri);
    webSocket.onOpen.listen((e) {
      log.info("Websocket opened with $uri.");
      completer.complete();
    });

    webSocket.onMessage.listen((MessageEvent e) {
      log.finest('Received message: ${e.data}}');
      var message = JSON.decode(e.data);
      var type = message['type'];
      var data = message['data'];
      switch (type) {
        case 'offer':
          handleOffer(new RtcSessionDescription(data));
          break;
        case 'answer':
          handleAnswer(new RtcSessionDescription(data));
          break;
        case 'ice-candidate':
          handleIceCandidate(new RtcIceCandidate(data));
          break;
        default:

      }
    });

    return completer.future;
  }

  send(data) {
    if (webSocket != null && webSocket.readyState == WebSocket.OPEN) {
        log.finest("Sending to signal server: $data");
        webSocket.send(data);
    } else {
      log.severe('WebSocket not connected, message $data not sent');
    }
  }

  sendIceCandidate(RtcIceCandidate iceCandidate) {
    send(encodedIceCandidate(iceCandidate));
  }

  offer(RtcSessionDescription offer) {
    send(encodeSessionDescription('media.webrtc.offer', offer));
  }

  encodedIceCandidate(RtcIceCandidate iceCandidate) {
    var message = {'type': 'media.webrtc.ice-candidate', 'data': {
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

  void subscribe() {
    // TODO: Replace with actual identifier!
    var msg = {'type': 'media.webrtc.subscribe', 'data': {'identifier': 1}};
    send(JSON.encode(msg));
  }
}

class WebRtcSignalModule extends Module {
  WebRtcSignalExampleModule() {
    bind(WebRtcSignalService);
  }
}
