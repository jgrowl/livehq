library webrtc_signal.publisher_factory;

import 'dart:html';
import 'dart:async';

import 'package:logging/logging.dart';
import 'package:observe/observe.dart';
import 'package:di/annotations.dart';

import 'publisher.dart';
import 'web_rtc_config.dart';
import 'identifier_resolver.dart';
import 'signal_handler.dart';

@Injectable()
class PublisherFactory {
  final Logger log = new Logger('PublisherFactory');

  WebRtcConfig webRtcConfig;
  SignalHandler _signalHandler;
  IdentifierResolver _identifierResolver;

  PublisherFactory(this._signalHandler, this._identifierResolver, this.webRtcConfig);

  Future<Publisher> createPublisher(List<MediaStream> mediaStreams) {
    var completer = new Completer<Publisher>();
    _identifierResolver.create().then((identifier) {
      completer.complete(new Publisher(identifier, mediaStreams, _signalHandler, webRtcConfig));
    });

    return completer.future;
  }

}
