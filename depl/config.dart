import 'dart:io';
import 'package:path/path.dart' as p;

enum Command {
  build,
  clean;

  static Command of(List<String> args) {
    if (args.isEmpty) {
      return build;
    }
    switch (args[0].trim().toLowerCase()) {
      case "build":
        return build;
      case "clean":
        return clean;
      default:
        return build;
    }
  }
}

class Config {
  final Command command;
  final Directory buildDir;
  final bool readonly;
  final bool noDocker;
  final String? imageSuffix;
  final String? database;

  Config(this.command, this.buildDir,
      {bool? readonly, bool? noDocker, this.imageSuffix, this.database})
      : readonly = readonly != null ? readonly : false,
        noDocker = noDocker != null ? noDocker : false;

  bool get hasDatabase => database != null;

  static Config parse(List<String> args) {
    var command = Command.of(args);
    print("run build of type: ${command.name}");
    var readonly = false;
    var noDocker = false;
    Directory? dir = null;
    String? suffix = null;
    for (int i = 0; i < args.length; i++) {
      var arg = args[i];
      if (arg == "--readonly") {
        readonly = true;
        continue;
      }
      if (arg == "--no-docker") {
        noDocker = true;
        continue;
      }
      if (arg.startsWith("-d") && i < args.length - 1) {
        dir = _buildDirOf(args[i + 1]);
        i++;
        continue;
      }
      if (arg.startsWith("-i") && i < args.length - 1) {
        suffix = args[i + 1];
        i++;
        continue;
      }
    }
    dir = dir != null ? dir : _buildDirOf("build");
    print("build in: ${dir.path}");
    return Config(command, dir,
        imageSuffix: suffix,
        database: _dbOf(dir),
        readonly: readonly,
        noDocker: noDocker);
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
