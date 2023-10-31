FROM gcr.io/distroless/java17-debian12@sha256:cd099373e0e21eb374fba4e136f660af65176a92553bfb6b69c9d6f05e25ae67

ENV JDK_JAVA_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+UseParallelGC -XX:ActiveProcessorCount=2"

COPY build/libs/app.jar /app/
WORKDIR /app
CMD ["app.jar"]
