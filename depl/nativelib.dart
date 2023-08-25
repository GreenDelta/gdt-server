import 'dart:io';
import 'package:archive/archive.dart';
import 'package:archive/archive_io.dart' as zipio;

import 'config.dart';

const version = "0.0.1";

clean(Config config) async {
  final _zip = "native_${config.os.name}.zip";
  var zip = config.fileOf(_zip);
  if (await zip.exists()) {
    await zip.delete();
  }
}

  syncLibsWith(Config config) async {
  print("sync native libraries ...");

  // check if the library folder already exists
  final libDir =
      config.dirOf("native/olca-native/$version/${config.os.name}/" + "x64");
  if (libDir.existsSync()) {
    print("  native library folder exists: ${libDir.path}");
    return;
  }

  // download the libraries if necessary
  final _zip = "native_${config.os.name}.zip";
  final libZip = config.fileOf(_zip);
  if (!libZip.existsSync()) {
    final url = "https://github.com/GreenDelta/olca-native/releases/" +
        "/download/v$version/olca-native-umfpack-${config.os.name}-x64.zip";
    var request = await HttpClient().getUrl(Uri.parse(url));
    var response = await request.close();
    await response.pipe(libZip.openWrite());
  }

  // extract the library zip
  print("  extract native libraries");
  print(libZip.path);
  final buffer = zipio.InputFileStream(libZip.path);
  final zip = ZipDecoder().decodeBuffer(buffer);
  for (var file in zip.files) {
    if (!file.isFile) continue;
    final name = file.name.split("/").last;
    final target = File(libDir.path + "/" + name);
    if (target.existsSync()) continue;
    final out = zipio.OutputFileStream(target.path);
    file.writeContent(out);
    out.close();
  }
  buffer.close();

  print("  ok");
}
