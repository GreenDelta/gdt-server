import "nativelib.dart" as nativelib;
import "app.dart" as app;
import "docker.dart" as docker;
import 'config.dart';

main(List<String> args) async {
  var config = Config.parse(args);
  switch (config.command) {
    case Command.build:
      print("run build in folder: ${config.buildDir}");
      await app.syncApp(config);
      await nativelib.syncLibsWith(config);
      docker.build(config);
      break;
    case Command.clean:
      print("clean build in folder: ${config.buildDir}");
      await app.clean(config);
      await nativelib.clean(config);
      docker.clean(config);
      break;
  }
}
