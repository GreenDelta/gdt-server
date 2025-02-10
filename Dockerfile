FROM ghcr.io/greendelta/gdt-server-app AS app
FROM ghcr.io/greendelta/gdt-server-lib AS lib
FROM ghcr.io/greendelta/gdt-server-native AS native
FROM eclipse-temurin:21-jre

ENV JAVA_MAX_RAM_PERCENTAGE=80

COPY --from=app /app /app
COPY --from=lib /app/lib /app/lib
COPY --from=native /app/native /app/native

RUN chmod +x /app/run.sh

ENTRYPOINT ["/app/run.sh"]
