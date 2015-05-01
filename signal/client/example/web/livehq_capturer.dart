import 'package:webrtc_signal/webrtc_signal.dart';

import 'package:polymer/polymer.dart';
import 'package:paper_elements/paper_toast.dart';
//import 'package:observe/observe.dart';

@CustomTag('livehq-capturer')
class LiveHqCapturer extends PolymerElement {
  @published Capturer capturer;

  LiveHqCapturer.created() : super.created();

  void capture() {
    capturer.capture();
    // TODO: This should only happen when capture is successful
    PaperToast captureStarted = this.shadowRoot.querySelector("#capture-started");
    captureStarted.show();
  }
}