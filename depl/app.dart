import 'dart:io';

import 'config.dart';

syncApp(Config config) async {
  var build = _AppBuild(config);
  await build.run();
}

clean(Config config) async {
  var files = [config.fileOf("gdt-server.jar"), config.fileOf("run.sh")];
  for (var file in files) {
    if (await file.exists()) {
      await file.delete();
    }
  }
  var libDir = config.dirOf("lib");
  if (await libDir.exists()) {
    await libDir.delete(recursive: true);
  }
}

class _AppBuild {
  final Config config;

  _AppBuild(this.config);

  run() async {
    print("build the application ...");
    await _compile();
    await _syncFiles();
    await _generateRunScript();
    print("  ok");
  }

  _compile() async {
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
  }

  _syncFiles() async {
    print("  sync files");
    var appJar = config.fileOf("gdt-server.jar");
    if (appJar.existsSync()) {
      appJar.delete();
    }
    await File("target/gdt-server.jar").copy(appJar.path);

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
        await File(f.path).copy(lib.path);
      }
      syncedLibs.add(path);
    }

    // delete old library files
    for (var f in libDir.listSync()) {
      var name = f.path.split("/").last;
      var path = config.pathOf("lib/$name");
      if (!syncedLibs.contains(path)) {
        print("  delete old lib $path");
        await File(f.path).delete();
      }
    }
  }

  _generateRunScript() async {
    var text = "#!/bin/bash\njava -jar gdt-server.jar -port 8080 "
        "-timeout 30 -native native";
    if (config.hasDatabase) {
      text += " -data data -db ${config.database}";
    }
    if (config.readonly) {
      text += " --readonly";
    }
    await config.fileOf("run.sh").writeAsString(text + "\n");
  }
}
