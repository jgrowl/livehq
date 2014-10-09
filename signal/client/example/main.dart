import 'package:angular/angular.dart';
import 'package:angular/application_factory.dart';
import 'dart:js';
import 'dart:async';
import 'dart:convert' show JSON;
import 'dart:html';

import 'package:webrtc-signal/webrtc_signal.dart';

@Controller(
    selector: '[app]',
    publishAs: 'ctrl')
class AppController {
  WebRtcSignalService _webRtcSignalService;
  AppController(this._webRtcSignalService) {
//    print('hithere');
  }

}

class WebRtcSignalExampleModule extends Module {
  WebRtcSignalExampleModule() {
    bind(AppController);
    bind(WebRtcSignalService);
  }
}

void main() {
//  Logger.root.level = Level.FINEST;
//  Logger.root.onRecord.listen((LogRecord r) { print(r.message); });

  Injector injector = applicationFactory()
  .addModule(new WebRtcSignalModule())
  .addModule(new WebRtcSignalExampleModule())
  .run();
}

