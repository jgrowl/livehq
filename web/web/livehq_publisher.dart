import 'dart:html';
import 'package:polymer/polymer.dart';
import 'package:observe/observe.dart';
import 'package:logging/logging.dart';

@CustomTag('livehq-publisher')
class LiveHqPublisher extends PolymerElement {
  final Logger log = new Logger('LiveHqPublisher');

  LiveHqPublisher.created() : super.created();

  publish(Event e, var detail, Node sender) {
    log.finest("Publishing MediaStreams");
    dispatchEvent(new CustomEvent('publish'));
  }
}