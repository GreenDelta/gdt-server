import "dart:io";

import "nativelib.dart" as nativelib;

main() {
  var buildDir = Directory("build");
  if (!buildDir.existsSync()) {
    buildDir.createSync();
  }
  nativelib.syncLibsWith(buildDir);
}
