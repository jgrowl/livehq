library angular.webrtc.signal.service;

import 'dart:js';
import 'dart:async';
import 'dart:html';
import 'dart:convert' show JSON;

import 'package:angular/angular.dart';

@Injectable()
class WebRtcSignalService {
  WebSocket webSocket;
  Function handleAnswer;
  Function handleIceCandidate;


  WebRtcSignalService() {
    initializeWebSocket();
  }

  initializeWebSocket() {
    webSocket = new WebSocket('ws://127.0.0.1:1234/ws');
    webSocket.onOpen.listen((e) {
      print('WebSocket opened...');
    });

    webSocket.onMessage.listen((MessageEvent e) {
      var message = JSON.decode(e.data);
      var type = message['type'];
      var data = message['data'];
      switch (type) {
        case 'answer':
          handleAnswer(data);
          break;
        case 'ice-candidate':
          handleIceCandidate(data);
          break;
        default:

      }
    });
  }

  send(data) {
    if (webSocket != null && webSocket.readyState == WebSocket.OPEN) {
        print('sending...');
        webSocket.send(data);
    } else {
      print('WebSocket not connected, message $data not sent');
    }
  }

  offer(RtcSessionDescription offer) {
    send(encodeSessionDescription('media.webrtc.offer', offer));
  }

  createPeerConnection() {
    var msg = {'type': 'media.webrtc.create-peerconnection', 'data': {}};
    send(JSON.encode(msg));
  }

  encodeSessionDescription(String type, RtcSessionDescription s) {
    return JSON.encode({'type': type, 'data': {'sdp': s.sdp, 'type': s.type}});
  }
}

class WebRtcSignalModule extends Module {
  WebRtcSignalExampleModule() {
    bind(WebRtcSignalService);
  }
}
