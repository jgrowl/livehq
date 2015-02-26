part of webrtc.signal;

abstract class IdentifierResolver {
  Future<String> create();
}

class RestfulIdentifierResolver extends IdentifierResolver {
  Future<String> create() {
    var completer = new Completer<String>();
    var url = "http://localhost:3000/api/v1/identifiers.json";
//    var data = { 'firstName' : 'John', 'lastName' : 'Doe' };
    var data = { };
    HttpRequest.postFormData(url, data).then((HttpRequest request) {
      // Do something with the response.
      Map json = JSON.decode(request.response);
      var identifier = json['identifier']['id'];
      completer.complete(identifier.toString());
    });

    return completer.future;
  }
}
