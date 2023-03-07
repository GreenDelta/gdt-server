# gdt-server

`gdt-server` is server application that implements Rest-API of the openLCA IPC
protocol. See the [openLCA IPC
documentation](https://greendelta.github.io/openLCA-ApiDoc/ipc/) for more
information and examples.

## Running as Docker container

A Docker image of the gdt-server can be composed with [this Docker
file](./Dockerfile); e.g. if you have curl and Docker installed:

```bash
cd <workdir>
curl https://raw.githubusercontent.com/GreenDelta/gdt-server/main/Dockerfile \
  > Dockerfile \
  && docker build -t gdt-server .
```
Then you can run a container in the following way:

```bash
docker run \
  -p 3000:8080 \
  -v $HOME/openLCA-data-1.4:/app/data \
  --rm -d gdt-server \
  -db example --readonly
```

In this example, it will mount the default openLCA workspace to the container,
use the database `example`, and start the server in readonly mode on port 3000.
Other server parameters can be passed via this start command as described in the
openLCA IPC documentation. You can also host static files by mounting a folder
to the `/app/static` folder:

```
  -v <your host folder>:/app/static
```

## Building from source

In order to build the server application, the current version of the openLCA
Maven modules need to be installed, see
https://github.com/GreenDelta/olca-modules.

```bash
git clone https://github.com/GreenDelta/olca-modules.git
cd olca-modules
mvn install -DskipTests=true
```

Application packages can be then build with the `depl` tool:

```bash
dart depl/main.dart
```

This builds the server application in the `build` folder, collects the
jar-dependencies and native libraries, and also creates 3 Docker images:

* `gdt-server-app`: contains the server application
* `gdt-server-lib`: contains the Java dependencies of the server application
* `gdt-server-native`: contains the native calculation libraries

These images can be combined to a server application using [this
Dockerfile](./Dockerfile). Note that in order to run the Docker builds you may need to
[configure Docker](https://docs.docker.com/engine/install/linux-postinstall/) so
that it doesn't need to run as root user.

It is also possible to build images that contain (read-only) databases with
LCA models by running the build with a build folder that contains a `data`
folder with the following layout:

```
+ <build folder>
  + data
    + databases
      - <openLCA database>
    + libraries
      - <possible data library>
      - ...
```

Other build options are:

* commands are `build` (default) or `clean`
* `-d <build folder>`: runs or cleans the build in the given folder
* `-i <image suffix>`: appends the suffix to the Docker image in case of a build
   with packaged database: `gdt-server-<image suffix>`
* `-port <port>`: set the server port that should be used in the generated scripts
* `--no-images`: create Docker files but do not create and delete images

You can also compile the `depl` tool and run the compiled version:

```bash
dart compile exe depl/main.dart -o depl/depl
sudo ./depl/depl docker
```

## License
The app layer with the REST API of the gdt-server (e.g. the `gdt-server-app`
Docker image) and the `depl` tool are licensed under the
_GNU Affero General Public License Version 3_ (AGPL-v3)
[see the license text](./LICENSE). The library layer (e.g. the `gdt-server-base`
Docker image) contains the openLCA core and other third party libraries with
different licenses, partly _not_ compatible with the AGPL-v3. This means, that
you can compose and use a server locally on your computer but distributing the
server is not allowed as it violates the conditions of the AGPL-v3. Note that
distributing according to the AGPL-v3, also includes usages over a network.
GreenDelta provides Docker images and gdt-server builds under different license
conditions than the AGPL-v3. If you need another license,
[just let us know](https://www.greendelta.com/about-us/contact-us/).
