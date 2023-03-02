from gdt-server-app as app
from gdt-server-lib as lib
from eclipse-temurin:17-jre

copy --from=app /app /app
copy --from=lib /app/lib /app/lib

run chmod +x /app/run.sh
entrypoint ["/app/run.sh"]
