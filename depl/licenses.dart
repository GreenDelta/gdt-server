import 'dart:io';

enum License {
  // order them in a way so that preferred licenses are on top in case of
  // libraries with multiple licenses
  Apache2("Apache License, Version 2.0"),
  BSD3("BSD 3 Clause License"),
  EPL2("Eclipse Public License 2.0"),
  EPL1("Eclipse Public License 1.0"),
  MIT("MIT License"),
  MPL2("Mozilla Public License, Version 2.0"),
  LGPL2_1("GNU Lesser General Public License, Version 2.1"),
  ;

  final String title;

  const License(this.title);

  static License? identify(String code) {
    var c = code.toLowerCase();
    if (c == "epl 2.0") return License.EPL2;

    if (c.contains("apache") && c.contains("2")) {
      return License.Apache2;
    }
    if (c.contains("bsd")) {
      if (c.contains("3") || c.contains("new")) {
        return License.BSD3;
      }
    }
    if (c.contains("eclipse")) {
      if (c.contains("public")) {
        if (c.contains("2.0")) {
          return License.EPL2;
        }
        if (c.contains("1.0")) {
          return License.EPL1;
        }
      }
      if (c.contains("distribution")) {
        return License.BSD3;
      }
    }
    if (c.contains("mit")) {
      return License.MIT;
    }
    if (c.contains("mozilla") && c.contains("2")) {
      return License.MPL2;
    }
    if (c.contains("gnu") && c.contains("lesser") && c.contains("2.1")) {
      return License.LGPL2_1;
    }

    return null;
  }

  static License? select(Info info) {
    License? selected = null;
    for (var code in info.licenses) {
      var l = License.identify(code);
      if (l == null) {
        stderr.write("WARNING: unknown license $code\n");
        continue;
      }
      if (selected == null) {
        selected = l;
        continue;
      }
      if (selected.index > l.index) {
        selected = l;
      }
    }
    return selected;
  }
}

class Info {
  final String id;
  final String project;
  final String url;
  final List<String> licenses;

  Info(this.id, this.project, this.url, this.licenses);

  String get version => id.split(":").last;

  static Info? parse(String line) {
    var licenses = <String>[];
    String info = "";
    line.split(")").map((e) => e.trim()).forEach((e) {
      if (e.startsWith("(")) {
        licenses.add(e.substring(1));
      } else if (e.length > 0) {
        info = e;
      }
    });
    var parts = info.split("(");
    if (parts.length < 2) return null;
    var project = parts[0].trim();
    parts = parts[1].split(" - ");
    if (parts.length < 2) return null;
    return Info(parts[0].trim(), project, parts[1].trim(), licenses);
  }
}

main() async {
  // await Process.run("mvn", ["license:add-third-party"], runInShell: true);
  var file = File("target/generated-sources/license/THIRD-PARTY.txt");
  var text = await file.readAsString();

  for (var line in text.split("\n")) {
    var infoStr = line.trim();
    if (!infoStr.startsWith("(")) continue;
    var info = Info.parse(infoStr);
    if (info == null) {
      print("error: failed to parse line $infoStr");
      continue;
    }

    var title = "${info.project} ${info.version}";
    print(title);
    var hline = "";
    for (int i = 0; i < title.length; i++) {
      hline += "-";
    }
    print(hline);
    var license = License.select(info);
    if (license == null) {
      stderr.write("WARNING could not identify license of ${info.id}\n");
      print("No license could be identified");
    } else {
      print("This library is licensed under the ${license.title}.");
      print("A copy of the license is attached below.\n");
    }
  }
}
