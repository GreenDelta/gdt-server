# creates the deployment packages

import os
import re
import shutil
import subprocess
import urllib.request
import zipfile

from pathlib import Path
import sys

DIR = Path(os.path.dirname(__file__))
DIST_DIR = DIR / 'dist'


def main():
    args = sys.argv
    if len(args) > 1:
        if  args[1] == 'clean':
            clean()
            return
        if args[1] == 'image':
            build_image()
            return

    print('create deployment packages')
    DIST_DIR.mkdir(parents=True, exist_ok=True)
    sync_jar()
    sync_native_libs()
    # build_image()


def clean():
    print('clean the build')
    if not DIST_DIR.exists():
        DIST_DIR.mkdir(parents=True, exist_ok=True)
    jar = DIST_DIR / 'gdt-server.jar'
    if jar.exists():
        print('  ... delete build')
        jar.unlink()


def sync_jar():
    jar_path = DIST_DIR / 'gdt-server.jar'
    if jar_path.exists():
        print('  gdt-server.jar exists')
        return

    print('  build gdt-server.jar')
    subprocess.call('mvn clean package -DskipTests=true', shell=True, cwd=DIR)
    shutil.copy2(DIR/'target/gdt-server.jar', jar_path)


def sync_native_libs():

    native_dir = DIST_DIR / 'native/olca-native/0.0.1/x64'
    if native_dir.exists():
        print('  native library folder exists')
        return

    native_zip_path = DIST_DIR / 'native_linux_x64.zip'
    if not native_zip_path.exists():
        url = 'https://github.com/GreenDelta/olca-native/releases/'\
            'download/v0.0.1/olca-native-umfpack-linux-x64.zip'
        print(f'  fetch native libraries from: {url}')
        urllib.request.urlretrieve(url, native_zip_path)

    print('  extract native libraries')
    native_dir.mkdir(parents=True)
    with zipfile.ZipFile(native_zip_path) as zf:
        handled = set()
        for name in zf.namelist():
            select = False
            if '.so' in name or name.endswith('.json') or 'LICENSE' in name:
                select = True
            if not select:
                continue
            target_name = name.split('/')[-1]

            # some packages contain the same file under different paths
            if target_name in handled:
                continue
            handled.add(target_name)

            with open(native_dir / target_name, 'wb') as f:
                f.write(zf.read(name))


def build_image():
    # delete a possible old image
    images = subprocess.check_output(
        'docker image ls', shell=True, cwd=DIR).decode('utf-8', errors='?')
    exists = False
    for line in images.splitlines():
        if line.startswith('gdt-server'):
            exists = True
            break
    if exists:
        print('delete old image:\n')
        subprocess.call('docker image rm gdt-server', shell=True, cwd=DIR)

    print('\nbuild new image:\n')
    subprocess.call('docker build -t gdt-server .', shell=True, cwd=DIR)
    # TODO: export the image
    print('done')

if __name__ == '__main__':
    main()
