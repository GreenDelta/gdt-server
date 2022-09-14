# gdt-server

`gdt-server` is a small server that provides a Rest-API for some openLCA core
functions like data management and calculations. It can be integrated as a
service in a larger application context.

## Running the server

The server is a Java application that connects to an openLCA database. It can
be configured by providing the following command line arguments:

```

gdt-server

Usage:

  java -jar gdt-server.jar <args>

Arguments:

  -data <path to data folder>

    Path to the data folder that contains the database, possible libraries etc.
    The folder structure should follow the openLCA workspace structure (with the
    databases and libraries in the respective sub-folders). If this option is
    not provided, it defaults to the respective default openLCA workspace
    location: ~/openLCA-data-1.4

  -db <database>

    The name of the database. This is the name of the respective folder in the
    data directory. This argument is required.

  -port <port>

    The port of the server. Defaults to 8080 if this option is not provided.

  -native <path to native library folder>

    The path to the folder from which the native libraries should be loaded.
    Defaults to the data folder if this path is not provided.

  -static <path to folder with static files>

    An optional path to a folder with static files that should be hosted by
    the server.

  --readonly <true | false>?

    If this flag is set, the server will run in readonly mode and modifying the
    database will not be possible via http requests.

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

## TODO:

* clean-up old results; e.g. when a new result is cached, check for old results
  that were not used for a specific time and remove them from the map
* allow put with JSON arrays: for inserting multiple data of a respective type

## API

### `GET /data/{type}`
* get the descriptors of the available data sets of the given type
  (which can be `process`, `flow`, etc.)

### `GET /data/{type}/{id}`
* returns the full data set of the given type and ID

### `GET /data/{type}/{id}/info`
* returns the descriptor of the data set of the given type and ID

### `GET /data/{type}/{id}/parameters`
* returns the parameters of the specified data set, this is only
  valid for data set types that can have parameters or parameter
  redefinitions

### `PUT /data/{type}`
* inserts or updates the provided data set in the database; this method
  is not available when the server runs in read-only mode

### `DELETE /data/{type}/{id}`
* deletes the specified data set from the database; this method is not
  available when the server runs in read-only mode

### `POST /results/calculate`
* start a calculation for the provided setup, returns the calculation
  state with the result ID

### `DELETE | POST /results/{id}/dispose`
* disposes the result with the given ID

### `GET /results/{id}/state`
* returns the calculation state of the result with the given ID

### `GET  /results/{id}/total-impacts`
* returns the total LCIA result
