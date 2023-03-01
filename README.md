# gdt-server

`gdt-server` is server application that implements Rest-API of the openLCA IPC
protocol. See the [openLCA IPC
documentation](https://greendelta.github.io/openLCA-ApiDoc/ipc/) for more
information and examples.

## Building

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
jar-dependencies and native libraries, and also creates 2 Docker images:

* `gdt-server-app`: contains the server application and native calculation
  libraries
* `gdt-server-base`: contains the dependencies of the server application

These images can be combined to a server application using [this
Dockerfile](./). Note that in order to run the Docker builds you may need to
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
* `--no-docker`: skips the docker build

You can also compile the `depl` tool and run the compiled version:

```bash
dart compile exe depl/main.dart -o depl/depl
sudo ./depl/depl docker
```

## Running as Docker container

When you run the server as Docker container, you need to mount a workspace
folder that contains the database and possible libraries to the `/app/data`
folder of the container. The name of the database and possible other arguments
are passed to the container at the end of the command:

```batch
sudo docker run \
  -p 8080:8080 \
  -v $HOME/openLCA-data-1.4:/app/data \
  --rm -d gdt-server \
  -db ei22 --readonly
```

You can also host static files by mounting a folder to the `/app/static` folder:

```
  -v <your host folder>:/app/static
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
