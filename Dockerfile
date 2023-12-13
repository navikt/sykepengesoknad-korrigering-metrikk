FROM gcr.io/distroless/java17-debian12@sha256:76ac1768c577881e3cc04a4da5bdfa650c1ae5129ecdc9b5367d61881eb05b79

ENV JDK_JAVA_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+UseParallelGC -XX:ActiveProcessorCount=2"

COPY build/libs/app.jar /app/
WORKDIR /app
CMD ["app.jar"]
