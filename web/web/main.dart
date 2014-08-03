import 'package:angular/angular.dart';
import 'package:angular/application_factory.dart';
import 'dart:js';

//import 'oauth.dart';

void main() {
  applicationFactory().run();
  var OAuth = context['OAuth'];
  OAuth.callMethod('initialize', ['otTvGcYtLMK1Q6W6d8LHeQlO4lo']);

  String provider = 'twitter';

  var opts = new JsObject.jsify({
      'state': 8
  });

  var popup = OAuth.callMethod('popup', [provider, opts])
  .callMethod('done', [(result) {
    print('success yo');
    print(result);}
  ]).callMethod('fail', [(err) {
    print('failed yo');
    print(err);
  }]);

}

