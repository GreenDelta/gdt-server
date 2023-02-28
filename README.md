# gdt-server

`gdt-server` is server application that implements Rest-API of the
[openLCA IPC protocol](https://greendelta.github.io/openLCA-ApiDoc/ipc/). See
the openLCA IPC documentation for the available functions and examples.

## Building

In order to build the server application, the current version of  the openLCA
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

This builds the server application, collects the jar-dependencies and native
libraries. With the `docker` command, you can build the different docker
images (note that you may have to do this as `sudo` command with the current
user path):

```bash
sudo env "PATH=$PATH" dart depl/main.dart docker
```

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
