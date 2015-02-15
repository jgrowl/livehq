import 'dart:html';
import 'dart:async';

import 'package:logging/logging.dart';

import 'package:webrtc-signal/webrtc_signal.dart';

void main() {
  Logger.root.onRecord.listen((LogRecord r) { print(r.message); });
Logger.root.level = Level.FINEST;
final Logger log = new Logger('Main');
  Future<WebSocket> initWebSocket() {
    Completer completer = new Completer();

    var uri = 'ws://127.0.0.1:1234/ws';
    var webSocket = new WebSocket(uri);
    webSocket.onOpen.listen((e) {
    log.info("Websocket opened with $uri.");
      completer.complete(webSocket);
    });

    return completer.future;
  }

  initWebSocket().then((webSocket) {
    var signalHandler = new WebSocketSignalHandler.fromWebSocket(webSocket);
    var publisher = new Publisher(signalHandler);
    var subscriber = new Subscriber(signalHandler);

    ButtonElement startCamera = query("#start-camera");
    startCamera.onClick.listen((event) => publisher.createMediaStream());

//    var video = new VideoElement()
//      ..autoplay = true
//      ..src = Url.createObjectUrl(stream)
//      ..onLoadedMetadata.listen((e) => print(e));
//    document.body.append(video);

    ButtonElement publish = query("#publish");
    publish.onClick.listen((event) => publisher.publishStreams());


    ButtonElement subscribe = query("#subscribe");
    subscribe.onClick.listen((event) => subscriber.subscribe("1"));


//    // Testing code
//    VideoElement pc1Video = query("#pc1_video");

//      String url = Url.createObjectUrl(e.stream);
//      pc1Video.src = url;
  });

}
