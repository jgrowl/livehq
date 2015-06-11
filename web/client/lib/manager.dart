library webrtc_signal.manager;

import 'package:json_object/json_object.dart';
import 'webrtc_signal.dart';


@Injectable()
class Manager extends Observable {
  final Logger log = new Logger('Manager');

  Capturer capturer;
//  List<Publisher> publishers = new List<Publisher>();
  Publisher publisher;
  PublisherFactory publisherFactory;
  SubscriberFactory subscriberFactory;
  List<Subscriber> subscribers = toObservable([]);
  List<JsonObject> availablePcs = toObservable([]);

  // TODO: Put this in config with a cat
//  String peerConnectionsUrl = 'http://localhost:3000/api/v1/streams';

  String get host {
    return window.location.hostname;
  }

  String get peerConnectionsUrl {
    return "http://$host:3000/api/v1/streams";
//    return "http://$host:3000/api/v1/identifiers.json";
  }


  Manager(this.capturer, this.publisherFactory, this.subscriberFactory) {
    log.finest("Initializing Manager.");
  }

  void publish() {
    publisherFactory.createPublisher(capturer.mediaStreams).then((publisher) {
      this.publisher = publisher;
      this.publisher.publishStreams();
    });
  }

  void setAvailablePcs(JsonObject pcs) {
    this.availablePcs.clear();
    this.availablePcs.addAll(pcs);
  }

  void createSubscriber(String identifier) {
      subscriberFactory.createSubscriber(identifier).then((Subscriber subscriber) {
       subscribers.add(subscriber);
       subscriber.subscribe();
      });
  }
}
