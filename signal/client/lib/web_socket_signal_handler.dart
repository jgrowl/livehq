part of webrtc.signal;

class WebSocketSignalHandler extends SignalHandler {
  final Logger log = new Logger('WebSocketSignalHandler');
  WebSocket webSocket;

  WebSocketSignalHandler.fromWebSocket(this.webSocket) {
    webSocket.onMessage.listen((MessageEvent e) {
      log.finest('Received message: ${e.data}}');
      var message = JSON.decode(e.data);
      onSignal(message);
    });
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
