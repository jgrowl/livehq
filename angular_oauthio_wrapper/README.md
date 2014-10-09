angular_oauthio_wrapper.dart
============================

An AngularDart service wrapper around OAuth.io's oauth.js library


Bind your config in your module, ie:

```dart
import 'package:angular_oauthio_wrapper/angular_oauthio_wrapper.dart' show OauthioConfig, OmniauthOauthioConfig;
class YourModule extends Module {
  YourModule() {
    String publicKey = 'YOUR_PUBLIC_KEY';
    bind(OauthioConfig, toValue: new OmniauthOauthioConfig(publicKey, 'localhost', 3000, 'users/auth')..secure=false);
  }
}
```

Note that I am using using my provided OmniauthOauthioConfig that can be used with the rails 
[omniauth-oauthio](https://github.com/jgrowl/omniauth-oauthio) provider that I created. I added an abstract 
`OauthioConfig` class that can be implemented if you need custom configuration.
