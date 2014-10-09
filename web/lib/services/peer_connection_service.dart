library livehq.services.peer_connection;

import 'dart:js';
import 'dart:async';
import 'dart:html';
import 'dart:convert' show JSON;

import 'package:angular/angular.dart';
import 'package:webrtc-signal/webrtc_signal.dart';


@Injectable()
class PeerConnectionService {
  var _iceServers = {
      'iceServers': [{
          'url': 'stun:stun.l.google.com:19302'
      }]
  };

  var _dataConfig = {
      'optional': [{
          'RtpDataChannels': true
      }, {
          'DtlsSrtpKeyAgreement': false   // TODO: Secure this?
      }]
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
  var _constraints = {
      'optional': [], 'mandatory': {
          'OfferToReceiveAudio': true, 'OfferToReceiveVideo': true
      }
  };

  WebRtcSignalService _signalService;

  RtcPeerConnection _peerConnection;

  PeerConnectionService(this._signalService) {
    _signalService.handleIceCandidate = (RtcIceCandidate iceCandidate) {
      _peerConnection.addIceCandidate(iceCandidate);
    };

    _signalService.handleAnswer = (RtcSessionDescription answer) {
      _peerConnection.setRemoteDescription(answer);
    };

  }

  sendMedia() {
    _peerConnection = _createPeerConnection();

    _signalService.createPeerConnection();
    _createOffer(_peerConnection).then((RtcSessionDescription offer) {
      _signalService.offer(offer);
    });
  }

  _createPeerConnection() {
    var pc = new RtcPeerConnection(_iceServers, _dataConfig);
//
//    pc.onIceCandidate.listen((e){
//      if (e.candidate != null) {
//        _send('candidate', {
//            'label': e.candidate.sdpMLineIndex,
//            'id': id,
//            'candidate': e.candidate.candidate
//        });
//      }
//    });
//
//    pc.onAddStream.listen((e) {
//      _messageController.add({
//          'type': 'add',
//          'id': id,
//          'stream': e.stream
//      });
//    });
//
//    pc.onRemoveStream.listen((e) {
//      _messageController.add({
//          'type': 'remove',
//          'id': id,
//          'stream': e.stream
//      });
//    });
//
//    pc.onDataChannel.listen((e) {
//      _addDataChannel(id, e.channel);
//    });

    return pc;
  }


  Future<RtcSessionDescription> _createOffer(RtcPeerConnection pc) {
    return pc.createOffer(_constraints).then((RtcSessionDescription s) {
      pc.setLocalDescription(s);
      return s;

//      var test = {
//        'description': {
//          'sdp': s.sdp,
//          'type': s.type
//        }
//      };
//      print(JSON.encode(test));

//      _signalService.offer(s);

//      _send('offer', {
//          'id': socket,
//          'description': {
//              'sdp': s.sdp,
//              'type': s.type
//          }
//      });
    });
  }

}

