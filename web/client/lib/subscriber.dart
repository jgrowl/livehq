library webrtc_signal.subscriber;

import 'dart:html';
import 'dart:async';

import 'package:logging/logging.dart';
import 'signal_handler.dart';
import 'web_rtc_config.dart';
import 'webrtc_signal.dart';

class Subscriber {
  static const offer = 'web.subscriber.webrtc.offer';
  static const candidate = 'web.subscriber.webrtc.ice-candidate';
  static const answer = 'web.subscriber.webrtc.answer';
  static const subscribeSignal = 'media.subscriber.webrtc.subscribe';

  final Logger log = new Logger('Subscriber');

  SignalHandler _signalHandler;

  WebRtcConfig webRtcConfig;

  List<MediaStream> _mediaStreams = toObservable([]);

  List<MediaStream> get mediaStreams => _mediaStreams;

  RtcPeerConnection _peerConnection;

  String identifier;

  String publisherIdentifier;

  Subscriber(this.identifier, this.publisherIdentifier, this._signalHandler, [this.webRtcConfig]) {
    log.info("Initializing Subscriber($identifier)...");

    if (this.webRtcConfig == null) {
      this.webRtcConfig = new WebRtcConfig();
    }

    _peerConnection = _createPeerConnection();

    _listenToMessages();
  }

  void _listenToMessages() {
    _signalHandler.onMessage.listen((Message message) {
      if (message.identifier == identifier) {

//      log.finest("Subscriber Message ${message.type} received. [${message.data}]}");
      switch (message.type) {
        case offer:
          RtcSessionDescription offer = new RtcSessionDescription(message.data);
          _createAnswer(_peerConnection, offer).then((RtcSessionDescription answer) {
            _signalHandler.subscriberAnswer(identifier, answer);
          });
          break;
        case candidate:
          RtcIceCandidate iceCandidate = new RtcIceCandidate(message.data);
          _peerConnection.addIceCandidate(iceCandidate, () {
            log.fine("IceCandidate added successfully.");
          }, (String error) {
            log.severe("Error adding IceCandidate! [$error]");
          });
          break;
        case answer:
          RtcSessionDescription answer = new RtcSessionDescription(message.data);
          _peerConnection.setRemoteDescription(answer);
          break;
        default:
      }
    }
    });
    log.finest("Subscriber($identifier) listening for messages.");
  }

  _createPeerConnection() {
    var pc = new RtcPeerConnection(webRtcConfig.iceServers, webRtcConfig.dataConfig);
    pc.onIceCandidate.listen((e) {
      if (e.candidate != null) {
        _signalHandler.sendSubscriberIceCandidate(identifier, e.candidate);
      }
    });

    pc.onAddStream.listen((MediaStreamEvent e) {
      log.info("Adding stream(${e.stream.id})");
      _mediaStreams.add(e.stream);
    });

    pc.onRemoveStream.listen((e) {
      log.info("Removing stream ${e.stream.id}");
      _mediaStreams.remove(e.stream);
    });

//    pc.onDataChannel.listen((e) {
//      _addDataChannel(id, e.channel);
//    });

    return pc;
  }

  Future<RtcSessionDescription> _createOffer(RtcPeerConnection pc) {
    return pc.createOffer(webRtcConfig.constraints).then((RtcSessionDescription s) {
      pc.setLocalDescription(s);
      return s;
    });
  }

  Future<RtcSessionDescription> _createAnswer(RtcPeerConnection pc, RtcSessionDescription offer) {
    return pc.setRemoteDescription(offer).then((thingy){
      return pc.createAnswer(webRtcConfig.constraints).then((RtcSessionDescription s) {
        pc.setLocalDescription(s);
        return s;
      });
    });
  }

  void subscribe() {
    _signalHandler.send(Signal.encoded(identifier, subscribeSignal, {'publisherIdentifier': publisherIdentifier}));
  }

  void close() {
    // TODO: return future
    _mediaStreams.forEach((mediaStream) => mediaStream.stop());
    _peerConnection.close();
  }
}
