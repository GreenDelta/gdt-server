FROM eclipse-temurin:17-jre

# we copy everything to the /app folder
RUN mkdir -p /app

# copy the fat-jar of the server into the `/app` older of the image
COPY dist/gdt-server.jar /app

# copy the native calculation libraries into the `/home/lib` folder
COPY dist/native /app/native

COPY start_container.sh /app
RUN chmod +x /app/start_container.sh

ENTRYPOINT ["/app/start_container.sh"]

# build the docker image just via
# sudo docker build -t gdt-server .

# start a container via; change the paths and port so that it matches your setup
# sudo docker run -p 8080:8080 -v $HOME/openLCA-data-1.4:/app/data -d gdt-server -db ei22 --readonly
