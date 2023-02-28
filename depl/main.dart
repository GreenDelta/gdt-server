import "nativelib.dart" as nativelib;
import "app.dart" as app;
import "docker.dart" as docker;
import 'config.dart';

main(List<String> args) async {
  var config = Config.parse(args);
  switch (config.command) {
    case Command.app:
      await app.syncApp(config);
      await nativelib.syncLibsWith(config);
      break;
    case Command.docker:
      docker.build(config);
      break;
  }
}
