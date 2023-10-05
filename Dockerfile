FROM gcr.io/distroless/java17-debian12@sha256:82ec33483644e853dad0f5e6956c675be47ba6de00a8f74dadee321b6b0febb5

ENV JDK_JAVA_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+UseParallelGC -XX:ActiveProcessorCount=2"

COPY build/libs/app.jar /app/
WORKDIR /app
CMD ["app.jar"]
