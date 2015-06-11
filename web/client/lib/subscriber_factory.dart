library webrtc_signal.subscriber_factory;

import 'webrtc_signal.dart';

@Injectable()
class SubscriberFactory {
  final Logger log = new Logger('SubscriberFactory');

  WebRtcConfig webRtcConfig;

  SignalHandler _signalHandler;
  IdentifierResolver _identifierResolver;

  SubscriberFactory(this._signalHandler, this._identifierResolver, [this.webRtcConfig]) {
    if (this.webRtcConfig == null) {
      this.webRtcConfig = new WebRtcConfig();
    }
  }

  Future<Subscriber> createSubscriber(String publisherIdentifier) {
    var completer = new Completer<Subscriber>();

    _identifierResolver.create().then((identifier) {
      completer.complete(new Subscriber(identifier, publisherIdentifier, _signalHandler, webRtcConfig));
    });

    return completer.future;
  }

  DelayedSubscriber createDelayedSubscriber(String publisherIdentifier) {
    return new DelayedSubscriber(publisherIdentifier);
  }

}

class DelayedSubscriber {

  String publisherIdentifier;

  DelayedSubscriber(this.publisherIdentifier);


  void create() {



  }

  
}
