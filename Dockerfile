FROM gcr.io/distroless/java17-debian12@sha256:8f80873debfafc0b77dd22d03e6d34d0a716c52404ca4ca19f76f2de11000c55

ENV JDK_JAVA_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+UseParallelGC -XX:ActiveProcessorCount=2"

COPY build/libs/app.jar /app/
WORKDIR /app
CMD ["app.jar"]
