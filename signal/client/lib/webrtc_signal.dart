library webrtc_signal;

import 'dart:html';
import 'dart:async';
import 'dart:convert' show JSON;
import 'package:observe/observe.dart';
import 'package:logging/logging.dart';
import 'package:di/di.dart';
import 'package:di/annotations.dart';

// Why are we importing/exporting? See https://code.google.com/p/dart/issues/detail?id=20314
import 'web_rtc_config.dart';
import 'capturer.dart';
import 'manager.dart';
import 'publisher.dart';
import 'subscriber.dart';
import 'web_socket_signal_handler.dart';
import 'signal_handler.dart';
import 'publisher_factory.dart';
import 'subscriber_factory.dart';
import 'identifier_resolver.dart';

export 'dart:html';
export 'dart:async';

export 'package:logging/logging.dart';
export 'package:observe/observe.dart';
export 'package:di/di.dart';
export 'package:di/annotations.dart';

export 'web_rtc_config.dart';
export 'capturer.dart';
export 'manager.dart';
export 'publisher.dart';
export 'subscriber.dart';
export 'web_socket_signal_handler.dart';
export 'signal_handler.dart';
export 'publisher_factory.dart';
export 'subscriber_factory.dart';
export 'identifier_resolver.dart';

class Signal {

  static String encoded(String identifier, String type, [Map data = const {}]) {
    var msg = {'identifier': identifier, 'type': type, 'data': data};
    return JSON.encode(msg);
  }
}

class Message {
  String identifier;
  String type;
  Map data;

  Message(this.identifier, this.type, this.data);
}

