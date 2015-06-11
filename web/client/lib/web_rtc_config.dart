library webrtc_signal.web_rtc_config;

import 'package:di/annotations.dart';

//@Injectable()
//class WebrtcConfig {
//  var iceServers = {
//      'iceServers': [{
//          'url': 'stun:stun.l.google.com:19302'
//      }]
//  };
//
//  var mediaConfig = {
//      'audio': false,
//      'video': true
//  };
//
//  Map dataConfig = {
//      'optional': [
//          {'RtpDataChannels': 'true'},
//          {'DtlsSrtpKeyAgreement': 'false'}
//      ]
//  };
//
///* dart2js doesn't do recursive convertDartToNative_Dictionary()
//   * and it fails in Chrome
//   * so I had to remove constraints for now
//  var _constraints = {
//    'optional': [],
//    'mandatory': {
//      'OfferToReceiveAudio': true,
//      'OfferToReceiveVideo': true
//    }
//  };
//  */
//  var constraints = {};
////  var constraints = {
////      'optional': [], 'mandatory': {
////          'OfferToReceiveAudio': true,
////          'OfferToReceiveVideo': true
////      }
////  };
//}

@Injectable()
class StandardWebRtcConfig implements WebRtcConfig {
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
  var constraints = {};
//  var constraints = {
//      'optional': [], 'mandatory': {
//          'OfferToReceiveAudio': true,
//          'OfferToReceiveVideo': true
//      }
//  };
}


abstract class WebRtcConfig {
  var iceServers;
  var mediaConfig;
  var dataConfig;
  var constraints;

}
