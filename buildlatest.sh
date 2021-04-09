echo "Bygger sykepengesoknad-korrigering-metrikk latest"

./gradlew bootJar

docker build . -t sykepengesoknad-korrigering-metrikk:latest
