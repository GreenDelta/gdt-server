import 'dart:io';
import 'package:path/path.dart' as p;
import 'config.dart';

const _app = """
from scratch
copy gdt-server.jar /app/gdt-server.jar
copy native /app/native
copy run.sh /app/run.sh
""";

const _lib = """
from scratch
copy lib /app/lib
""";

const _run = """#!/bin/bash
java -jar /app/gdt-server.jar \\
    -data /app/data \\
    -native /app/native \\
    -static /app/static \\
    "\$@"
""";

build(Config config) {
  var build = _DockerBuild(config);
  build.clean();
  if (config.hasDatabase) {
    build.dbImage();
  } else {
    build.layers();
  }
}

clean(Config config) {
  _DockerBuild(config).clean();
  _cleanFiles(config);
}

_cleanFiles(Config config) async {
  await for (var f in config.buildDir.list()) {
    if (p.basename(f.path).endsWith("Dockerfile")) {
      var file = File(f.path);
      await file.delete();
    }
  }
}

class _DockerBuild {
  final Config config;

  _DockerBuild(this.config);

  dbImage() {
    print("build docker image with database");
    var recipe = "from eclipse-temurin:17-jre\n"
        "copy gdt-server.jar /app/gdt-server.jar\n"
        "copy lib /app/lib\n"
        "copy native /app/native\n"
        "copy data /app/data\n"
        'cmd ["java", "-jar", "/app/gdt-server.jar", '
        '"-data", "/app/data", '
        '"-db", "${config.database}", '
        '"-native", "/app/native", '
        '"-port", "8080", '
        '"--readonly" ]\n';
    config.fileOf("Dockerfile").writeAsStringSync(recipe);
    var name = config.imageSuffix != null
      ? "gdt-server-${config.imageSuffix}"
      : "gdt-server";
    print("  build image $name");
    _docker(["build", "-t", "$name", "."]);
    print("  export image as $name.tar");
    _docker(["save", "-o", "$name.tar", "$name"]);
  }

  layers() {
    print("build docker images ...");
    print("  generate scripts");
    var mkFile = (String file, String content) =>
        config.fileOf(file).writeAsStringSync(content);
    mkFile("app.Dockerfile", _app);
    mkFile("lib.Dockerfile", _lib);
    mkFile("main.Dockerfile", File("Dockerfile").readAsStringSync());
    mkFile("run.sh", _run);
    [
      ["app.Dockerfile", "gdt-server-app"],
      ["lib.Dockerfile", "gdt-server-lib"],
      ["main.Dockerfile", "gdt-server"]
    ].forEach((p) => _buildImage(config.buildDir, p[0], p[1]));
  }

  _buildImage(Directory buildDir, String file, String tag) {
    print("  build image $tag");
    _docker(["build", "-t", tag, "-f", file, "."]);
  }

  clean() {
    print("clean up docker artifacts");

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

  ProcessResult _docker(List<String> args) {
    final pr =
        Process.runSync("docker", args, workingDirectory: config.buildDir.path);
    if (pr.exitCode != 0) {
      print(pr.stderr);
      print("ERROR: command failed: docker ${args.join(' ')}");
      exit(pr.exitCode);
    }
    return pr;
  }
}
