import "dart:io";

import "nativelib.dart" as nativelib;
import "app.dart" as app;
import "docker.dart" as docker;

main(List<String> args) async {
  String buildTarget;
  if (args.isEmpty) {
    print("info: no build target provided; default to 'app'");
    buildTarget = 'app';
  } else {
    buildTarget = args[0];
  }

  var buildDir = Directory("build");
  if (!buildDir.existsSync()) {
    buildDir.createSync();
  }

  switch (buildTarget) {
    case "app":
      await app.syncApp(buildDir);
      await nativelib.syncLibsWith(buildDir);
      break;
    case "docker":
      docker.buildImages(buildDir);
      break;
    default:
      print("error: unknown build target: $buildTarget");
  }
}
