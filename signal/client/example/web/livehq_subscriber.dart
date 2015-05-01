import 'dart:html';
import 'package:polymer/polymer.dart';
import 'package:logging/logging.dart';
//import 'package:webrtc_signal/webrtc_signal.dart';

@CustomTag('livehq-subscriber')
class LiveHqSubscriber extends PolymerElement {
  final Logger log = new Logger('LiveHqSubscriber');

  String identifier;

  LiveHqSubscriber.created() : super.created();

  void subscribe(Event e, var detail, Node sender) {
    log.finest("Dispatching subscribe event ($identifier)");
    dispatchEvent(new CustomEvent('subscribe', detail: {'identifier': identifier}));
  }
}