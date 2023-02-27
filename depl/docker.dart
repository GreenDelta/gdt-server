import 'dart:io';

const _base = """
FROM eclipse-temurin:17-jre

RUN mkdir -p /app
COPY lib /app/lib
""";

const _app = """
FROM gdt-server-base

COPY native /app/native
COPY gdt-server.jar /app

COPY ../start_container.sh /app
RUN chmod +x /app/start_container.sh

ENTRYPOINT ["/app/start_container.sh"]
""";

buildImages(Directory buildDir) {
  File(buildDir.path + "/base.Dockerfile").writeAsString(_base);
  File(buildDir.path + "/app.Dockerfile").writeAsStringSync(_app);
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
