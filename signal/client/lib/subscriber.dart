part of webrtc.signal;

class Subscriber {
  final Logger log = new Logger('Subscriber');

  SignalHandler _signalHandler;

  WebRtcConfig webRtcConfig;

  List<MediaStream> _mediaStreams = new List<MediaStream>();

  RtcPeerConnection _peerConnection;

  Subscriber(this._signalHandler, {this.webRtcConfig}) {
    log.finest("Creating subscriber...");

    if (this.webRtcConfig == null) {
      this.webRtcConfig = new WebRtcConfig();
    }

    _peerConnection = _createPeerConnection();

    _listenToMessages();
  }

  void _listenToMessages() {
    _signalHandler.onMessage.listen((Message message) {
//      log.finest("Subscriber Message ${message.type} received. [${message.data}]}");
      switch (message.type) {
        case Signal.subscriberOffer:
          RtcSessionDescription offer = new RtcSessionDescription(message.data);
          _createAnswer(_peerConnection, offer).then((RtcSessionDescription answer){
            _signalHandler.subscriberAnswer(answer);
          });
          break;
        case Signal.subscriberCandidate:
          RtcIceCandidate iceCandidate = new RtcIceCandidate(message.data);
          _peerConnection.addIceCandidate(iceCandidate, () {}, (String error) {
            log.severe("Error adding IceCandidate: $error");
          });
          break;
        case Signal.subscriberAnswer:
          RtcSessionDescription answer = new RtcSessionDescription(message.data);
          _peerConnection.setRemoteDescription(answer);
          break;
        default:
      }});
    log.finest("Subscriber Now listening for messages.");
  }

  _createPeerConnection() {
    var pc = new RtcPeerConnection(webRtcConfig.iceServers, webRtcConfig.dataConfig);
    pc.onIceCandidate.listen((e) {
      if (e.candidate != null) {
        _signalHandler.sendSubscriberIceCandidate(e.candidate);
      }
    });

    pc.onAddStream.listen((MediaStreamEvent e) {
      log.info("Adding stream(${e.stream.id})");
      _mediaStreams.add(e.stream);

//      // This is debug code. The user should be able to specify what happens
//      VideoElement pc1Video = query("#pc1_video");
//      String url = Url.createObjectUrl(e.stream);
//      pc1Video.src = url;


    var video = new VideoElement()
      ..autoplay = true
      ..src = Url.createObjectUrl(e.stream)
      ..onLoadedMetadata.listen((e) => print("Event: ${e.type}"));
    document.body.append(video);


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

  void subscribe(String identifier) {
    _signalHandler.send(Signal.encoded(Signal.subscribe, {'identifier': identifier}));
  }
}
