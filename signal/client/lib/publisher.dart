part of webrtc.signal;

class Publisher {
  final Logger log = new Logger('Publisher');

  SignalHandler _signalHandler;

  WebRtcConfig webRtcConfig;

  List<MediaStream> _mediaStreams = new List<MediaStream>();

  RtcPeerConnection _peerConnection;

  String _identifier;

  Publisher(this._identifier, this._signalHandler, [this.webRtcConfig]) {
    log.finest("Initializing Publisher($_identifier).");

    if (this.webRtcConfig == null) {
      this.webRtcConfig = new WebRtcConfig();
    }

    _peerConnection = _createPeerConnection();

    _listenToMessages();
  }

  void _listenToMessages() {
    _signalHandler.onMessage.listen((Message message) {
//      log.finest("Message ${message.type} received. [${message.data}]}");
      switch (message.type) {
        case 'offer':
          RtcSessionDescription offer = new RtcSessionDescription(message.data);
          _createAnswer(_peerConnection, offer);
          break;
        case 'ice-candidate':
          RtcIceCandidate iceCandidate = new RtcIceCandidate(message.data);
          _peerConnection.addIceCandidate(iceCandidate, () {}, (String error) {
            log.severe("Problem adding IceCandidate: $error");
          });
          break;
        case 'answer':
          RtcSessionDescription answer = new RtcSessionDescription(message.data);
          _peerConnection.setRemoteDescription(answer);
          break;
        default:
      }});
    log.finest("Now listening for messages.");
  }

  publishStreams() {
    _peerConnection.addStream(_mediaStreams[0]);
    _createOffer(_peerConnection).then((RtcSessionDescription offer) {
      _signalHandler.offer(offer);
    });
  }

  void createMediaStream() {
    window.navigator.getUserMedia(audio: this.webRtcConfig.mediaConfig['audio'],
    video: webRtcConfig.mediaConfig['video']).then((stream) {
      log.info("Adding MediaStream: [${stream.label}}");
      _mediaStreams.add(stream);
    }).catchError((issue) => log.severe(issue));
  }

  _createPeerConnection() {
    var pc = new RtcPeerConnection(webRtcConfig.iceServers, webRtcConfig.dataConfig);
    pc.onIceCandidate.listen((e) {
      if (e.candidate != null) {
        _signalHandler.sendIceCandidate(e.candidate);
      }
    });

    pc.onAddStream.listen((MediaStreamEvent e) {
      log.warning("onAddStream should not get called from a Publisher!");
    });

    pc.onRemoveStream.listen((e) {
      log.warning("onRemoveStream should not get called from a Publisher!");
    });

    pc.onIceConnectionStateChange.listen((Event e) {
      log.info("Publisher.onIceCOnnectionStateChange: ${e.type}");
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
        return s;
      });
    });
  }
}
