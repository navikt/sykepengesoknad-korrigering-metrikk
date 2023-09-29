FROM gcr.io/distroless/java17-debian12@sha256:5626fefd568faa244e0597439a2c87b86ce0cb40eada2948e6f131a3bab59f37

ENV JDK_JAVA_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+UseParallelGC -XX:ActiveProcessorCount=2"

COPY build/libs/app.jar /app/
WORKDIR /app
CMD ["app.jar"]
