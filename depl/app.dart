import 'dart:io';

import 'config.dart';

syncApp(Config config) async {
  print("build the application ...");

  // compile the application
  print("  compile with Maven");
  var r = await Process.run("mvn", ["clean", "package", "-DskipTests=true"]);
  var stdout = r.stdout.toString();
  var buildSuccess = false;
  for (var line in stdout.split("\n")) {
    if (line.trim() == "[INFO] BUILD SUCCESS") {
      buildSuccess = true;
      break;
    }
  }
  if (!buildSuccess) {
    print(stdout);
    print("error: build failed");
    exit(1);
  }

  // sync app file
  print("  sync files");
  var appJar = config.fileOf("gdt-server.jar");
  if (appJar.existsSync()) {
    appJar.delete();
  }
  File("target/gdt-server.jar").copy(appJar.path);

  // sync lib files
  var libDir = config.dirOf("lib");
  if (!libDir.existsSync()) {
    libDir.createSync();
  }
  var syncedLibs = <String>[];
  for (var f in Directory("target/lib").listSync()) {
    var name = f.path.split("/").last;
    var path = config.pathOf("lib/$name");
    var lib = File(path);
    if (!lib.existsSync()) {
      print("  copy lib ${name}");
      File(f.path).copySync(lib.path);
    }
    syncedLibs.add(path);
  }

  // delete old library files
  for (var f in libDir.listSync()) {
    var name = f.path.split("/").last;
    var path = config.pathOf("lib/$name");
    if (!syncedLibs.contains(path)) {
      print("  delete old lib $path");
      File(f.path).deleteSync();
    }
  }
  print("  ok");
}
