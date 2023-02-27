import 'dart:io';

const _app = """
from scratch

copy gdt-server.jar /app/gdt-server.jar
copy native /app/native
""";

const _base = """
from eclipse-temurin:17-jre

copy lib /app/lib
copy run.sh /app/run.sh
run chmod +x /app/run.sh
""";

const _main = """
from gdt-server-app

from gdt-server-base

copy --from=0 /app/gdt-server.jar /app
copy --from=0 /app/native /app/native

entrypoint ["/app/run.sh"]
""";

const _run = """#!/bin/bash
java -jar /app/gdt-server.jar \
    -data /app/data \
    -native /app/native \
    -static /app/static \
    "\$@"
""";

buildImages(Directory buildDir) {
  print("build images ...");

  print("  clean up");
  clean();

  print("  generate scripts");
  var mkFile = (String file, String content) =>
      File(buildDir.path + "/" + file).writeAsStringSync(content);
  mkFile("app.Dockerfile", _app);
  mkFile("base.Dockerfile", _base);
  mkFile("main.Dockerfile", _main);
  mkFile("run.sh", _run);
  [
    ["app.Dockerfile", "gdt-server-app"],
    ["base.Dockerfile", "gdt-server-base"],
    ["main.Dockerfile", "gdt-server"]
  ].forEach((p) => _buildImage(buildDir, p[0], p[1]));
}

_buildImage(Directory buildDir, String file, String tag) {
  print("  build image $tag");
  var o = Process.runSync("docker", ["build", "-t", tag, "-f", file, "."],
      workingDirectory: buildDir.path);
  if (o.exitCode != 0) {
    print(o.stderr);
    print("ERROR: failed to build image $tag from ${file}");
    exit(o.exitCode);
  }
}

clean() {
  // todo stop & delete containers

  // delete stopped containers
  final ws = RegExp("\\s+");
  var stdout = Process.runSync("docker", ["ps", "-a"]).stdout.toString();
  for (var line in stdout.split("\n")) {
    var parts = line.trim().split(ws);
    if (parts.length < 2) continue;
    var containerId = parts[0].trim();
    var image = parts[1].trim();
    if (image.startsWith("gdt-server")) {
      print("  delete container ${containerId} (image: ${image})");
      var code = Process.runSync("docker", ["rm", containerId]).exitCode;
      if (code != 0) {
        print("ERROR: failed to delete container $containerId");
        exit(code);
      }
    }
  }

  // delete images
  stdout = Process.runSync("docker", ["image", "ls"]).stdout.toString();
  for (var line in stdout.split("\n")) {
    var parts = line.trim().split(ws);
    if (parts.length < 2) continue;
    var image = parts[0].trim();
    if (image.startsWith("gdt-server")) {
      print("  delete image ${image}");
      var code = Process.runSync("docker", ["image", "rm", image]).exitCode;
      if (code != 0) {
        print("ERROR: failed to delete image $image");
        exit(code);
      }
    }
  }
}
