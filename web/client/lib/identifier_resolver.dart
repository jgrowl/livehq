library webrtc_signal.identifier_resolver;

import 'package:webrtc_signal/webrtc_signal.dart';

import 'dart:html';
import 'dart:async';
import 'dart:convert' show JSON;

@Injectable()
abstract class IdentifierResolver {
  Future<String> create();
}

@Injectable()
class RestfulIdentifierResolver extends IdentifierResolver {
  String uri;

  RestfulIdentifierResolver(this.uri);

  Future<String> create() {
    var completer = new Completer<String>();

//    var data = { 'firstName' : 'John', 'lastName' : 'Doe' };
    var data = { };
    HttpRequest.postFormData(uri, data).then((HttpRequest request) {
      // Do something with the response.
      Map json = JSON.decode(request.response);
      var identifier = json['identifier']['id'];
      completer.complete(identifier.toString());
    });

    return completer.future;
  }
}
