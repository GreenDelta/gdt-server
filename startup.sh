#!/bin/bash
java -cp "/app/lib/*:/app/gdt-server.jar" org.openlca.gdt.server.Server -data /app/data -native /app/native -static /app/static "$@"