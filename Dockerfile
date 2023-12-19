FROM gcr.io/distroless/java17-debian12@sha256:3da8c1021317fa0b0eca2d7bc9f54a1d09517fad07587abda0e1231bff207795

ENV JDK_JAVA_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+UseParallelGC -XX:ActiveProcessorCount=2"

COPY build/libs/app.jar /app/
WORKDIR /app
CMD ["app.jar"]
