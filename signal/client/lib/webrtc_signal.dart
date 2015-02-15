library webrtc.signal;

import 'dart:html';
import 'dart:async';
import 'dart:convert' show JSON;

import 'package:logging/logging.dart';

part 'signal_handler.dart';
part 'web_socket_signal_handler.dart';
part 'publisher.dart';
part 'subscriber.dart';


class Signal {
  static const subscribe = 'media.subscriber.webrtc.subscribe';

  static const subscriberOffer = 'web.subscriber.webrtc.offer';
  static const subscriberCandidate = 'web.subscriber.webrtc.ice-candidate';
  static const subscriberAnswer = 'web.subscriber.webrtc.answer';

  static String encoded(String type, Map data) {
    var msg = {'type': type, 'data': data};
    return JSON.encode(msg);
  }
}

class Message {

//  static const subscriberOffer = 'subscriber-offer';
//  static const subscriberCandidate = 'subscriber-candidate';
//  static const subscriberAnswer = 'subscriber-answer';


  String type;
  Map data;

  Message(this.type, this.data);
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


