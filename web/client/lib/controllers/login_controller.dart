library login_controller;

import 'package:angular/angular.dart';
import 'package:angular/application_factory.dart';
import 'dart:js';
import 'dart:async';
import 'dart:convert' show JSON;
import 'dart:html';

import 'package:satellizer/satellizer.dart';
import 'package:web/routes/auth_routes.dart' show authRouteInitializer;

class LoginForm {

}

@Controller(
    selector: '[login]',
    publishAs: 'ctrl')
class LoginController {
  final Router _router;
  final Auth _auth;

  LoginForm loginForm;

  LoginController(this._router, this._auth) {}

  void authenticate(String provider) {
    _auth.authenticate(provider)
    .then((result) => login(result))
    .catchError((e) {
      print('therewasanerror');
      print(e);
    });
  }

  void login(result) {
    if(result['status'] == 'additional_info_required') {
      _router.go('signup', {});
    } else if(result['status'] == 'signed_in') {
      _router.go('root', {});
    }
  }

}

