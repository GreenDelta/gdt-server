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

enum Os {
  linux,
  macos;

  static Os of(String value) {
    switch (value.trim().toLowerCase()) {
      case "linux":
        return linux;
      case "macos":
        return macos;
      case "mac":
        return macos;
      default:
        return linux;
    }
  }
}

class Config {
  final Command command;
  final Directory buildDir;
  final Os os;
  final bool readonly;
  final bool noImages;
  final String? imageSuffix;
  final String? database;
  final String? _port;

  Config(this.command, this.buildDir, this.os,
      {bool? readonly,
      bool? noImages,
      this.imageSuffix,
      this.database,
      String? port})
      : readonly = readonly != null ? readonly : false,
        noImages = noImages != null ? noImages : false,
        _port = port;

  bool get hasDatabase => database != null;
  String get port => _port ?? "8080";

  static Config parse(List<String> args) {
    var command = Command.of(args);
    print("run command of type: ${command.name}");

    // config
    var readonly = false;
    var noImages = false;
    Os os = Os.linux;
    Directory? dir = null;
    String? suffix = null;
    String? port = null;

    for (int i = 0; i < args.length; i++) {
      var arg = args[i];

      // boolean flags
      if (arg == "--readonly") {
        readonly = true;
        continue;
      }
      if (arg == "--no-images") {
        noImages = true;
        continue;
      }

      // key-value pairs
      if (!arg.startsWith("-") || i >= (args.length - 1)) {
        continue;
      }
      var value = args[i + 1];
      switch (arg) {
        case "-os":
          os = Os.of(value);
          break;
        case "-d":
          dir = _buildDirOf(value);
          break;
        case "-i":
          suffix = value;
          break;
        case "-port":
          port = value;
          break;
      }
      i++;
    }
    dir = dir != null ? dir : _buildDirOf("build");
    print("build in: ${dir.path}");
    return Config(command, dir, os,
        imageSuffix: suffix,
        database: _dbOf(dir),
        readonly: readonly,
        noImages: noImages,
        port: port);
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
