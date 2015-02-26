import 'dart:html';
import 'dart:async';

import 'package:logging/logging.dart';

import 'package:webrtc-signal/webrtc_signal.dart';

void main() {
  Logger.root.onRecord.listen((LogRecord r) {
    print("${r.time}\t${r.toString()}");
  });
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

    var identifierResolver = new RestfulIdentifierResolver();
    var factory = new PublisherSubscriberFactory(signalHandler, identifierResolver);

    factory.createPublisher().then((publisher) {
      ButtonElement startCamera = query("#start-camera");
      startCamera.onClick.listen((event) => publisher.createMediaStream());

      ButtonElement publish = query("#publish");
      publish.onClick.listen((event) => publisher.publishStreams());

      factory.createSubscriber(publisher.identifier).then((subscriber) {
        ButtonElement subscribe = query("#subscribe");
        subscribe.onClick.listen((event) => subscriber.subscribe());
      });
    });


//    var video = new VideoElement()
//      ..autoplay = true
//      ..src = Url.createObjectUrl(stream)
//      ..onLoadedMetadata.listen((e) => print(e));
//    document.body.append(video);

//    // Testing code
//    VideoElement pc1Video = query("#pc1_video");

//      String url = Url.createObjectUrl(e.stream);
//      pc1Video.src = url;
  });

}
