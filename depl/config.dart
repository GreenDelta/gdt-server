import 'dart:io';

enum Command {
  app,
  docker;

  static Command of(String arg) {
    switch (arg.trim().toLowerCase()) {
      case "app":
        return app;
      case "docker":
        return docker;
      default:
        print("error: unknown command: $arg");
        exit(1);
    }
  }
}

class Config {
  final Command command;
  final Directory buildDir;
  final bool readonly;
  final String? database;

  Config(this.command, this.buildDir, this.readonly, this.database);

  bool get hasDatabase => database != null;

  static Config parse(List<String> args) {
    if (args.isEmpty) {
      print("info: no build target provided; default to 'app'");
      var buildDir = _buildDirOf("build");
      return Config(Command.app, buildDir, false, _dbOf(buildDir));
    }

    var command = Command.of(args[0]);
    var readonly = false;
    Directory? dir = null;
    for (int i = 1; i < args.length; i++) {
      var arg = args[i];
      if (arg == "--readonly") {
        readonly = true;
        continue;
      }
      if (arg == "-d" && i < args.length - 2) {
        dir = _buildDirOf(args[i + 1]);
      }
    }
    dir = dir != null ? dir : _buildDirOf("build");
    return Config(command, dir, readonly, _dbOf(dir));
  }

  static Directory _buildDirOf(String path) {
    var dir = Directory(path);
    if (!dir.existsSync()) {
      dir.createSync(recursive: true);
    }
    return dir;
  }

  static String? _dbOf(Directory buildDir) {
    var dbDir = Directory(buildDir.path + "/data/databases");
    if (!dbDir.existsSync()) return null;
    for (var db in dbDir.listSync()) {
      var stat = db.statSync();
      if (stat.type != FileSystemEntityType.directory) {
        continue;
      }
      return db.uri.pathSegments.last;
    }
    return null;
  }

  String pathOf(String path) {
    var p = path.startsWith("/") ? path : "/" + path;
    return buildDir.path + p;
  }

  File fileOf(String path) {
    return File(pathOf(path));
  }

  Directory dirOf(String path) {
    return Directory(pathOf(path));
  }
}
