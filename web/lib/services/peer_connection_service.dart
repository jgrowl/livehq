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

  Map _dataConfig = {
      'optional': [
          {'RtpDataChannels': 'true'},
          {'DtlsSrtpKeyAgreement': 'false'}
      ]
  };

  var _mediaConfig = {
      'audio': false,
      'video': true
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

  List<MediaStream> _mediaStreams;

  WebRtcSignalService _signalService;

  RtcPeerConnection _peerConnection;

  PeerConnectionService(this._signalService) {
    // Request that the media server create a PeerConnection
    // TODO: Make the signal service return a 'PeerConnectionCreated' message before continuing
    _signalService.initialize();

    _mediaStreams = new List<MediaStream>();

    _peerConnection = _createPeerConnection();

    _signalService.handleIceCandidate = (RtcIceCandidate iceCandidate) {
      _peerConnection.addIceCandidate(iceCandidate, () {}, () {});
    };

    _signalService.handleAnswer = (RtcSessionDescription answer) {
      _peerConnection.setRemoteDescription(answer);
    };

    _signalService.handleOffer = (RtcSessionDescription offer) {
      _createAnswer(_peerConnection, offer);
    };
  }

  publishStreams() {
    _peerConnection.addStream(_mediaStreams[0]);
    _createOffer(_peerConnection).then((RtcSessionDescription offer) {
      _signalService.offer(offer);
    });
  }

  createMediaStream() {
    window.navigator.getUserMedia(audio: _mediaConfig['audio'], video: _mediaConfig['video']).then((stream) {
      _mediaStreams.add(stream);

      var video = new VideoElement()
        ..autoplay = true
        ..src = Url.createObjectUrl(stream)
        ..onLoadedMetadata.listen((e) => print(e));
      document.body.append(video);
    }).catchError(reportIssue);
  }

  reportIssue(issue) {
    print(issue);
  }

  _createPeerConnection() {

    var pc = new RtcPeerConnection(_iceServers, _dataConfig);
    pc.onIceCandidate.listen((e) {
      if (e.candidate != null) {
        _signalService.sendIceCandidate(e.candidate);
      }
    });

    // Testing code
    VideoElement pc1Video = query("#pc1_video");

    pc.onAddStream.listen((MediaStreamEvent e) {
      print("onAddStream");
      String url = Url.createObjectUrl(e.stream);
      pc1Video.src = url;
    });

    pc.onRemoveStream.listen((e) {
      print("onRemoveStream");
//      _messageController.add({
//          'type': 'remove',
//          'id': id,
//          'stream': e.stream
//      });
    });
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
    });
  }

  Future<RtcSessionDescription> _createAnswer(RtcPeerConnection pc, RtcSessionDescription offer) {
    return pc.setRemoteDescription(offer).then((thingy){
      return pc.createAnswer(_constraints).then((RtcSessionDescription s) {
        return s;
      });
    });
  }

  void subscribe() {
    _signalService.subscribe();
  }
}

