import 'package:polymer/polymer.dart';

import 'package:webrtc_signal/webrtc_signal.dart';

import 'livehq_capturer.dart';

@CustomTag('livehq-manager')
class LiveHqManager extends PolymerElement {
  final Logger log = new Logger('LiveHqManager');
  @published Manager manager;

  LiveHqManager.created() : super.created();

  void setManager(Manager manager) {
    this.manager = manager;

    LiveHqCapturer liveHqCapturer = this.shadowRoot.querySelector("livehq-capturer");
    liveHqCapturer.capturer = manager.capturer;

//    print(this.shadowRoot.querySelector("#publishButton"));

//    LiveHqPublisher liveHqPublisher = this.shadowRoot.querySelector("livehq-publisher");
//    print(liveHqPublisher);

//    var publishButton = this.shadowRoot.querySelector("livehq-publisher");
//    var publishButton = this.shadowRoot.querySelector("livehq-publisher");

//    var subscription = elem.onClick.listen(
//            (event) => print('click!'));
//
//    subscription.cancel();

//    print(this.shadowRoot.querySelector("livehq-publisher").shadowRoot.querySelector("paper-icon-button"));

  }

  void publish() {
    log.info("Publishing MediaStreams");
    manager.publish();
  }

  void subscribe(CustomEvent event) {
    var identifier = event.detail['identifier'];
    var subscriber = manager.subscribers.where((x) {
      return x.identifier == identifier;
    }).first;

   subscriber.subscribe();
  }

}