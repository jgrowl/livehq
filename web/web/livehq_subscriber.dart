import 'dart:html';
import 'package:polymer/polymer.dart';
import 'package:logging/logging.dart';
//import 'package:webrtc_signal/webrtc_signal.dart';

import 'package:webrtc_signal/webrtc_signal.dart';

@CustomTag('livehq-subscriber')
class LiveHqSubscriber extends PolymerElement {
  final Logger log = new Logger('LiveHqSubscriber');

  Subscriber subscriber;

  LiveHqSubscriber.created() : super.created();

  String createObjectUrl(MediaStream mediaStream) {
    return Url.createObjectUrl(mediaStream);
  }

  void close() {
    log.fine("Closing subscriber ${subscriber.identifier}");
    subscriber.close();
    dispatchEvent(new CustomEvent('close', detail: subscriber));
  }

//  void onLoadMetaData() {
//    log.fine("onLoadedMetaData: ${e.type} ");
//  }
}