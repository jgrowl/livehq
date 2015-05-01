library webrtc_signal.capturer;

import 'dart:html';
import 'dart:async';

import 'package:logging/logging.dart';
import 'package:observe/observe.dart';
import 'package:di/annotations.dart';

import 'web_rtc_config.dart';

@Injectable()
class Capturer extends Observable {
  final Logger log = new Logger('Capturer');

  WebRtcConfig webRtcConfig;

  @observable List<MediaStream> mediaStreams = toObservable([]);

  Capturer(this.webRtcConfig) {
    log.finest("Initializing Capturer...");
//    if (this.webRtcConfig == null) {
//      this.webRtcConfig = new StandardWebRtcConfig();
//    }
  }

//  Capturer([this.webRtcConfig]) {
//    log.finest("Initializing Capturer...");
//
//    if (this.webRtcConfig == null) {
//      this.webRtcConfig = new StandardWebRtcConfig();
//    }
//  }


  Future<MediaStream> capture() {
    var completer = new Completer<MediaStream>();
    window.navigator.getUserMedia(audio: this.webRtcConfig.mediaConfig['audio'],
    video: webRtcConfig.mediaConfig['video']).then((stream) {
      log.info("Adding MediaStream: [${stream.label}}");
      mediaStreams.add(stream);
      completer.complete(stream);
    }).catchError((issue) {
      log.severe(issue);
      completer.completeError(issue);
    });

    return completer.future;
  }
}
