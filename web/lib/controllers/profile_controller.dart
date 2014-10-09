library profile_controller;

import 'package:angular/angular.dart';

import 'package:satellizer/satellizer.dart';

@Controller(
    selector: '[profile]',
    publishAs: 'ctrl')
class ProfileController {
  final Router _router;
  final Auth _auth;
  User currentUser;
  RootScope _rootScope;

  ProfileController(this._router, this._auth, this._rootScope) {
    currentUser = _auth.currentUser;
  }

}

