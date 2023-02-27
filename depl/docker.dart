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
java -jar /app/gdt-server.jar \\
    -data /app/data \\
    -native /app/native \\
    -static /app/static \\
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
  _eachLineOf(_docker(["ps"]), (line) {
    if (line.length < 2) return;
    final container = line.last.trim();
    final image = line[1].trim();
    if (image.startsWith("gdt-server")) {
      print("  stop container $container (image: $image)");
      _docker(["stop", container]);
    }
  });

  // delete stopped containers
  _eachLineOf(_docker(["ps", "-a"]), (line) {
    if (line.length < 2) return;
    var containerId = line[0].trim();
    var image = line[1].trim();
    if (image.startsWith("gdt-server")) {
      print("  delete container ${containerId} (image: ${image})");
      _docker(["rm", containerId]);
    }
  });

  // delete images
  _eachLineOf(_docker(["image", "ls"]), (line) {
    if (line.length < 2) return;
    var image = line[0].trim();
    if (image.startsWith("gdt-server")) {
      print("  delete image ${image}");
      _docker(["image", "rm", image]);
    }
  });
}

ProcessResult _docker(List<String> args) {
  final pr = Process.runSync("docker", args);
  if (pr.exitCode != 0) {
    print(pr.stderr);
    args.join(" ");
    print("ERROR: command failed: docker ${args.join(' ')}");
    exit(pr.exitCode);
  }
  return pr;
}

_eachLineOf(ProcessResult pr, Function(List<String>) fn) {
  final out = pr.stdout;
  if (out == null) {
    return;
  }
  final ws = RegExp("\\s+");
  for (var line in out.toString().split("\n")) {
    var parts = line.trim().split(ws);
    fn(parts);
  }
}
