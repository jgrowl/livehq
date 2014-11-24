import 'package:angular/angular.dart';
import 'package:angular/application_factory.dart';
import 'package:logging/logging.dart';

import 'package:angular_oauthio_wrapper/angular_oauthio_wrapper.dart';
import 'package:satellizer/satellizer.dart';
import 'package:webrtc-signal/webrtc_signal.dart';

import 'package:web/services/peer_connection_service.dart';
import 'package:web/controllers/app_controller.dart';

import 'package:web/controllers/login_controller.dart';
import 'package:web/controllers/profile_controller.dart';
import 'package:web/controllers/registrations_controller.dart';
import 'package:web/routes/auth_routes.dart';

class WebModule extends Module {
  WebModule() {
    bind(AuthConfig, toValue: new AuthConfig()
      ..base='http://localhost:3000'
      ..createPath='/users.json'
      ..profilePath='/profile.json'
      ..apiPrefix='/api/v1'
    );
    bind(Auth);
    bind(WebRtcSignalService);
    bind(PeerConnectionService);
    bind(AppController);

    String publicKey = 'otTvGcYtLMK1Q6W6d8LHeQlO4lo';
    bind(OauthioConfig, toValue: new OmniauthOauthioConfig(publicKey, 'localhost', 3000, 'users/auth')..secure=false);

    bind(RouteInitializerFn, toImplementation: AuthRouteInitializer);
    bind(LoginController);
    bind(ProfileController);
    bind(RegistrationsController);
    bind(NgRoutingUsePushState,  toValue: new NgRoutingUsePushState.value(false));
  }
}

void main() {
  Logger.root.level = Level.FINEST;
  Logger.root.onRecord.listen((LogRecord r) {
    print(r.message);
  });

  Injector injector = applicationFactory()
  .addModule(new AuthModule())
  .addModule(new WebRtcSignalModule())
  .addModule(new WebModule())
  .run();
}

