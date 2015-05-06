import 'dart:html';
import 'package:polymer/polymer.dart';
import 'package:logging/logging.dart';
import 'package:json_object/json_object.dart';
//import 'package:webrtc_signal/webrtc_signal.dart';

@CustomTag('livehq-peer-connection-description')
class LiveHqPeerConnectionDescription extends PolymerElement {
  final Logger log = new Logger('LiveHqSubscriber');

 JsonObject peerConnectionDescription;

  LiveHqPeerConnectionDescription.created() : super.created();

  void subscribe(Event e, var detail, Node sender) {
    log.finest("Dispatching subscribe event ($identifier)");
    dispatchEvent(new CustomEvent('subscribe', detail: {'identifier': identifier}));
  }

  String get identifier => peerConnectionDescription.id;
}