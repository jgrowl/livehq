import 'dart:io';
import 'package:http_server/http_server.dart';
//import 'package:http_server/http_server.dart' show VirtualDirectory;

echo(HttpRequest req) {
  print('received submit');
  HttpBodyHandler.processRequest(req).then((HttpBody body) {
    print(body.body.runtimeType); // Map
    req.response
      ..headers.add('Access-Control-Allow-Origin', '*')
      ..headers.add('Content-Type', 'text/plain')
      ..statusCode = 201
      ..write(body.body.toString())
      ..close();
  });
}

VirtualDirectory virDir;

void directoryHandler(dir, request) {
  var indexUri = new Uri.file(dir.path).resolve('index.html');
  virDir.serveFile(new File(indexUri.toFilePath()), request);
}

main() {

  virDir = new VirtualDirectory(Platform.script.resolve('../web').toFilePath())
    ..allowDirectoryListing = true
    ..directoryHandler = directoryHandler;

  HttpServer.bind('0.0.0.0', 8080).then((HttpServer server) {
    print('Server is running');
    server.listen((HttpRequest req) {
      virDir.serveRequest(req);
    });
  });
}