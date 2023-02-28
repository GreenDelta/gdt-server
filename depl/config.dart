import 'dart:io';
import 'package:path/path.dart' as p;

enum Command {
  app,
  docker;

  static Command of(List<String> args) {
    if (args.isEmpty) {
      return app;
    }
    switch (args[0].trim().toLowerCase()) {
      case "app":
        return app;
      case "docker":
        return docker;
      default:
        return app;
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
    var command = Command.of(args);
    print("run build of type: ${command.name}");
    var readonly = false;
    Directory? dir = null;
    for (int i = 0; i < args.length; i++) {
      var arg = args[i];
      if (arg == "--readonly") {
        readonly = true;
        continue;
      }
      if (arg.startsWith("-d") && i < args.length - 1) {
        dir = _buildDirOf(args[i + 1]);
      }
    }
    dir = dir != null ? dir : _buildDirOf("build");
    print("build in: ${dir.path}");
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
      var dbName = p.basename(db.path);
      print("found database: $dbName");
      return dbName;
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
