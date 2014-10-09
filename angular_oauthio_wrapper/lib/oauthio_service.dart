library angular.oauthio_wrapper.service;

import 'dart:js';
import 'dart:async';
import 'dart:html';
import 'dart:convert' show JSON;

import 'package:angular/angular.dart';

abstract class OauthioConfig {
  bool secure           = true;
  bool withCredentials  = false;
  String publicKey;

  OauthioConfig(this.publicKey);

  String requestUrl(String provider);
  String callbackUrl(String provider, String code, String state);
  String get scheme => secure ? 'https' : 'http';
}

@Injectable()
class OmniauthOauthioConfig extends OauthioConfig {
  String base;                      // ie. localhost
  int port;                         // ie. 80
  String pathPrefix;                // ie. users/auth

  OmniauthOauthioConfig(String publicKey, this.base, this.port, this.pathPrefix) : super(publicKey);

  String requestUrl(String provider) {
    // ie. http://localhost:3000/users/auth/oauthio/twitter.json
    return "${scheme}://${base}:${port}/${pathPrefix}/oauthio/${provider}.json";
  }

  String callbackUrl(String provider, String code, String state) {
    // ie. http://localhost:3000/users/auth/oauthio/twitter/callback.json?code=1&state=2
    return "${scheme}://${base}:${port}/${pathPrefix}/oauthio/${provider}/callback.json?code=${code}&state=${state}";
  }
}

@Injectable()
class OauthioService {
  final OAuth = context['OAuth'];
  OauthioConfig _config;
  var result;

  OauthioService(this._config) {
    OAuth.callMethod('initialize', [_config.publicKey]);
  }

  Future popup(String provider) {
    return _request(provider)
    .then((optsWithState) => _popup(provider, optsWithState))
    .then((optsWithStateAndCode) => _callback(provider, optsWithStateAndCode));
  }

  Future _request(String provider) {
    var url = _config.requestUrl(provider);
    return HttpRequest.getString(url, withCredentials: _config.withCredentials).then((response){
      return JSON.decode(response);
    });
  }

  Future _popup(String provider, optsWithState) {
    Completer completer = new Completer();
    var popup = OAuth.callMethod('popup', [provider, new JsObject.jsify(optsWithState)]).callMethod('done', [(result) {
      this.result = result;
      context['result'] = result;
      optsWithState['code'] = result['code'];
      completer.complete(optsWithState);
    }]).callMethod('fail', [(err) {
      completer.completeError(err);
    }]);

    return completer.future;
  }

  Future _callback(String provider, optsWithStateAndCode) {
    var state = optsWithStateAndCode['state'];
    var code = optsWithStateAndCode['code'];
    var callbackUrl = _config.callbackUrl(provider, code, state);
    return HttpRequest.postFormData(callbackUrl, {}, withCredentials: _config.withCredentials)
    .then((HttpRequest request) {
      return request.response;
    });
  }
}

