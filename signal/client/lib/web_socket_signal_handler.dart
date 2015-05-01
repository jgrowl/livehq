//part of webrtc_signal;
library webrtc_signal.web_socket_signal_handler;
import 'dart:html';
import 'dart:convert' show JSON;

import 'package:logging/logging.dart';

import 'signal_handler.dart';

class WebSocketSignalHandler extends SignalHandler {
  final Logger log = new Logger('WebSocketSignalHandler');

  WebSocket webSocket;

  void initialize() {
    webSocket.onMessage.listen((MessageEvent e) {
      log.finest('Received message: ${e.data}}');
      var message = JSON.decode(e.data);
      onSignal(message);
    });
  }

  WebSocketSignalHandler.fromWebSocket(this.webSocket) {
    initialize();
  }

  void send(data) {
    if (webSocket != null && webSocket.readyState == WebSocket.OPEN) {
      log.finest("Sending to signal server: $data");
      webSocket.send(data);
    } else {
      log.severe('WebSocket not connected, message $data not sent');
    }
  }
}
