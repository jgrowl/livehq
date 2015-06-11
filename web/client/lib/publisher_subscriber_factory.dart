part of webrtc_signal;

class PublisherSubscriberFactory {
  final Logger log = new Logger('PublisherSubscriberFactory');

  WebRtcConfig webRtcConfig;

  SignalHandler _signalHandler;
  IdentifierResolver _identifierResolver;

  PublisherSubscriberFactory(this._signalHandler, this._identifierResolver, [this.webRtcConfig]) {
    if (this.webRtcConfig == null) {
      this.webRtcConfig = new WebRtcConfig();
    }
  }

  Future<Publisher> createPublisher() {
    var completer = new Completer<Publisher>();
    _identifierResolver.create().then((identifier) {
      completer.complete(new Publisher(identifier, _signalHandler, webRtcConfig));
    });

    return completer.future;
  }

  Future<Subscriber> createSubscriber(String publisherIdentifier) {
    var completer = new Completer<Subscriber>();

    _identifierResolver.create().then((identifier) {
      completer.complete(new Subscriber(identifier, publisherIdentifier, _signalHandler, webRtcConfig));
    });

    return completer.future;
  }

}
