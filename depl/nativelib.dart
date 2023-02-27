import 'dart:io';
import 'package:archive/archive.dart';
import 'package:archive/archive_io.dart' as zipio;

const version = "0.0.1";
const url = "https://github.com/GreenDelta/olca-native/releases/download/v" +
      "$version/olca-native-umfpack-linux-x64.zip";

syncLibsWith(Directory buildDir) {

  // check if the library folder already exists
  final libDir = Directory(buildDir.path + "/native/olca-native/$version/x64");
  if (libDir.existsSync()) {
    print("native library folder exists: ${libDir.path}");
    return;
  }

  // download the libraries if necessary
  final libZip = File(buildDir.path + "/native_linux_x64.zip");
  if (!libZip.existsSync()) {
    print("download native libraries");
  HttpClient().getUrl(Uri.parse(url))
    .then((request) => request.close())
    .then((response) => response.pipe(libZip.openWrite()));
  }

  // extract the library zip
  print("extract native libraries");
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
}
