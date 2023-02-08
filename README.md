# gdt-server

`gdt-server` is server application that implements Rest-API of the
[openLCA IPC protocol](https://greendelta.github.io/openLCA-ApiDoc/ipc/). See
the openLCA IPC documentation for the available functions and examples.

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
