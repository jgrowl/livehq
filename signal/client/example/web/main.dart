import 'dart:html';
import 'dart:async';

import 'package:di/di.dart';
import 'package:di/annotations.dart';
import 'package:logging/logging.dart';
import 'package:polymer/polymer.dart';
import 'package:core_elements/core_icon.dart';

import 'package:webrtc_signal/webrtc_signal.dart';

import 'livehq_manager.dart';


@Injectable()
class Config {
  final Logger log = new Logger('Config');

  WebSocket webSocket;

  Future init() async {
    Completer completer = new Completer();
    try {
      // Initialization logic
      webSocket = await initWebSocket();
      completer.complete();
    } catch(e) {
      completer.completeError(e);
    }

    return completer.future;
  }

  String get webSocketUri {
    // TODO: This shouldn't actually be tied to the current host!
    return "ws://${window.location.hostname}:1234/ws";
  }


  String get host {
    return window.location.hostname;
  }

  String get identifierResolverUri {
    return "http://$host:3000/api/v1/identifiers.json";
  }

  Future<WebSocket> initWebSocket() {
    Completer completer = new Completer();

    var webSocket = new WebSocket(webSocketUri);
    webSocket.onOpen.listen((e) {
      log.info("Websocket opened with $webSocketUri.");
      completer.complete(webSocket);
    });

    return completer.future;
  }

}

////  void test() async {
////    var publisher = await factory.createPublisher();
////
////    ButtonElement startCamera = query("#start-camera");
////    startCamera.onClick.listen((event) => publisher.createMediaStream());
////
////    ButtonElement publish = query("#publish");
////    publish.onClick.listen((event) => publisher.publishStreams());
////
////    var subscriber = await factory.createSubscriber(publisher.identifier);
////    ButtonElement subscribe = query("#subscribe");
////    subscribe.onClick.listen((event) => subscriber.subscribe());
////  }
//}

void main() async {
  Logger.root.onRecord.listen((LogRecord r) {
    print("${r.time}\t${r}");
  });

  Logger.root.level = Level.FINEST;
  final Logger log = new Logger('Main');

  Config config = new Config();
  await config.init();

  // See examples at https://github.com/angular/di.dart
  var injector = new ModuleInjector([new Module()
    ..bind(Config, toValue: config)
    ..bind(SignalHandler, toValue: new WebSocketSignalHandler.fromWebSocket(config.webSocket))
    ..bind(IdentifierResolver, toValue: new RestfulIdentifierResolver(config.identifierResolverUri))
    ..bind(WebRtcConfig, toImplementation: StandardWebRtcConfig)
    ..bind(Capturer)
    ..bind(PublisherFactory)
    ..bind(SubscriberFactory)
//    ..bind(PublisherSubscriberFactory)
    ..bind(Manager)

//    var machine = new PublisherSubscriberMachine(factory);
  ]);

  Manager manager = injector.get(Manager);

  var polymer = await initPolymer();
  polymer.run(() {
    // Code here is in the polymer Zone, which ensures that
    // @observable properties work correctly.
    Polymer.onReady.then((_) async {
      LiveHqManager liveHqManager = document.querySelector('livehq-manager');
      liveHqManager.setManager(manager);
    });
  });
}
