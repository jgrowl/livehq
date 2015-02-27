library webrtc.signal;

import 'dart:html';
import 'dart:async';
import 'dart:convert' show JSON;

import 'package:logging/logging.dart';

part 'signal_handler.dart';
part 'web_socket_signal_handler.dart';
part 'publisher.dart';
part 'subscriber.dart';
part 'publisher_subscriber_factory.dart';
part 'identifier_resolver.dart';


class Signal {

  static String encoded(String identifier, String type, [Map data = const {}]) {
    var msg = {'identifier': identifier, 'type': type, 'data': data};
    return JSON.encode(msg);
  }
}

class Message {
  String identifier;
  String type;
  Map data;

  Message(this.identifier, this.type, this.data);
}

class WebRtcConfig {
  var iceServers = {
      'iceServers': [{
          'url': 'stun:stun.l.google.com:19302'
      }]
  };

  var mediaConfig = {
      'audio': false,
      'video': true
  };

  Map dataConfig = {
      'optional': [
          {'RtpDataChannels': 'true'},
          {'DtlsSrtpKeyAgreement': 'false'}
      ]
  };

/* dart2js doesn't do recursive convertDartToNative_Dictionary()
   * and it fails in Chrome
   * so I had to remove constraints for now
  var _constraints = {
    'optional': [],
    'mandatory': {
      'OfferToReceiveAudio': true,
      'OfferToReceiveVideo': true
    }
  };
  */
//  var _constraints = {};
  var constraints = {
      'optional': [], 'mandatory': {
          'OfferToReceiveAudio': true,
          'OfferToReceiveVideo': true
      }
  };
}


