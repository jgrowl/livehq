part of webrtc.signal;

class PublisherSubscriberFactory {
  final Logger log = new Logger('PublisherSubscriberFactory');

  WebRtcConfig webRtcConfig;

  SignalHandler _signalHandler;

  PublisherSubscriberFactory(this._signalHandler, [this.webRtcConfig]) {
    if (this.webRtcConfig == null) {
      this.webRtcConfig = new WebRtcConfig();
    }
  }

  Publisher createPublisher(String identifier) {
    return new Publisher(identifier, _signalHandler, webRtcConfig);
  }

  Subscriber createSubscriber(String identifier) {
    return new Subscriber(identifier, _signalHandler, webRtcConfig);
  }

}
