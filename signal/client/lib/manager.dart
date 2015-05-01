library webrtc_signal.manager;

import 'webrtc_signal.dart';


@Injectable()
class Manager extends Observable {
  final Logger log = new Logger('Manager');

  Capturer capturer;

//  List<Publisher> publishers = new List<Publisher>();
  Publisher publisher;
  PublisherFactory publisherFactory;
  SubscriberFactory subscriberFactory;
//  List<Subscriber> subscribers = new List<Subscriber>();
  List<Subscriber> subscribers = toObservable([]);

  Manager(this.capturer, this.publisherFactory, this.subscriberFactory) {
    log.finest("Initializing Manager.");
  }

  void publish() {
    publisherFactory.createPublisher(capturer.mediaStreams).then((publisher) {
      this.publisher = publisher;

      subscriberFactory.createSubscriber(publisher.identifier).then((Subscriber subscriber) {
        subscribers.add(subscriber);
//        subscriber.subscribe();
      });

      this.publisher.publishStreams();
    });
  }
}
