FROM openjdk:17-alpine
RUN mkdir -p /data/heap-dumps
COPY CloudStore-0.0.1-SNAPSHOT.jar app.jar
CMD ["java", \
    "-XX:+HeapDumpOnOutOfMemoryError", \
    "-XX:HeapDumpPath=/data/heap-dumps", \
    "-jar", \
    "/app.jar"]
