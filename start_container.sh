#!/bin/bash

# folders must be mounted to the container; other parameters are passed as
# arguments to the container
java -jar /app/gdt-server.jar \
    -data /app/data \
    -native /app/native \
    -static /app/static \
    "$@"
