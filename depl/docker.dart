import 'dart:io';
import 'package:path/path.dart' as p;
import 'config.dart';

const _app = """
FROM scratch
COPY gdt-server.jar /app/gdt-server.jar
COPY run.sh /app/run.sh
COPY LICENSE /app/LICENSE
""";

const _lib = """
FROM scratch
COPY lib /app/lib
COPY licenses/lib.txt /app/THIRDPARTY_README
""";

const _native = """
FROM scratch
COPY native /app/native
COPY licenses/native.txt /app/THIRDPARTY_README
""";

const _run = """
#!/bin/bash
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
    print("generate Docker file");
    var recipe = "FROM eclipse-temurin:17-jre\n"
        "COPY gdt-server.jar /app/gdt-server.jar\n"
        "COPY lib /app/lib\n"
        "COPY native /app/native\n"
        "COPY data /app/data\n"
        'CMD ["java", "-jar", "/app/gdt-server.jar", '
        '"-data", "/app/data", '
        '"-db", "${config.database}", '
        '"-native", "/app/native", '
        '"-port", "${config.port}", '
        '"-timeout", "30", '
        '"--readonly" ]\n';
    config.fileOf("Dockerfile").writeAsStringSync(recipe);

    // build and export the image
    if (config.noImages) {
      return;
    }
    var name = config.imageSuffix != null
        ? "gdt-server-${config.imageSuffix}"
        : "gdt-server";
    print("build image $name");
    _docker(["build", "-t", "$name", "."]);
    print("export image as $name.tar");
    _docker(["save", "-o", "$name.tar", "$name"]);
  }

  layers() {
    print("generate Docker files");
    var mkFile = (String file, String content) {
      var f = config.fileOf(file);
      if (!f.parent.existsSync()) {
        f.parent.createSync(recursive: true);
      }
      f.writeAsStringSync(content);
    };
    mkFile("app.Dockerfile", _app);
    mkFile("lib.Dockerfile", _lib);
    mkFile("native.Dockerfile", _native);
    mkFile("main.Dockerfile", File("Dockerfile").readAsStringSync());
    mkFile("licenses/lib.txt", File("licenses/lib.txt").readAsStringSync());
    mkFile(
        "licenses/native.txt", File("licenses/native.txt").readAsStringSync());
    mkFile("LICENSE", File("LICENSE").readAsStringSync());
    mkFile("run.sh", _run);

    if (config.noImages) {
      return;
    }

    print("generate Docker images");
    var prefix = "ghcr.io/greendelta";
    [
      ["app.Dockerfile", "${prefix}/gdt-server-app"],
      ["lib.Dockerfile", "${prefix}/gdt-server-lib"],
      ["native.Dockerfile", "${prefix}/gdt-server-native"],
    ].forEach((p) {
      var file = p[0];
      var tag = p[1];
      print("  build image $tag");
      _docker(["build", "-t", tag, "-f", file, "."]);
    });
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
      if (image.contains("gdt-server")) {
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
