from ghcr.io/greendelta/gdt-server-app as app
from ghcr.io/greendelta/gdt-server-lib as lib
from ghcr.io/greendelta/gdt-server-native as native
from eclipse-temurin:17-jre

copy --from=app /app /app
copy --from=lib /app/lib /app/lib
copy --from=native /app/native /app/native

run chmod +x /app/run.sh
entrypoint ["/app/run.sh"]
