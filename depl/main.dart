import "dart:io";

import "nativelib.dart" as nativelib;
import "app.dart" as app;
import "docker.dart" as docker;


main() async {
  var buildDir = Directory("build");
  if (!buildDir.existsSync()) {
    buildDir.createSync();
  }

  // await app.syncApp(buildDir);
  // nativelib.syncLibsWith(buildDir);
  docker.clean();
}
