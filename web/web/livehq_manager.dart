import 'package:polymer/polymer.dart';
import 'package:json_object/json_object.dart';

import 'package:webrtc_signal/webrtc_signal.dart';

import 'livehq_capturer.dart';

@CustomTag('livehq-manager')
class LiveHqManager extends PolymerElement {
  final Logger log = new Logger('LiveHqManager');
  @published Manager manager;

//  String peerConnectionsUrl = 'http://localhost:3000/streams';

  LiveHqManager.created() : super.created();

  void setManager(Manager manager) {
    this.manager = manager;

    LiveHqCapturer liveHqCapturer = this.shadowRoot.querySelector("livehq-capturer");
    liveHqCapturer.capturer = manager.capturer;

    // For now we will just poll for updates
    const fiveSec = const Duration(seconds:5);
    new Timer.periodic(fiveSec, (Timer t) {
      log.finest("Updating available streams.");
      this.shadowRoot.querySelector("core-ajax-dart").go();
    });
  }

  void publish() {
    log.info("Publishing MediaStreams");
    manager.publish();
  }

  void subscribe(CustomEvent event) {
    var identifier = event.detail['identifier'];
    log.finest("Subscribing to $identifier");
    manager.createSubscriber(identifier);
  }

  void handlePeerConnectionsResponse(response) {
    JsonObject data = new JsonObject.fromMap(response.detail);
    var pcs = data.response.pcs;
    print(pcs.toString());
    manager.setAvailablePcs(pcs);
  }

  void close(CustomEvent event) {
    var subscriber = event.detail;
    manager.subscribers.remove(subscriber);
  }

  int get time {
    return new DateTime.now().millisecondsSinceEpoch;
  }
}