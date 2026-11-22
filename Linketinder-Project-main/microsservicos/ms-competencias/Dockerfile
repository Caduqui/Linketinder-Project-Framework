FROM eclipse-temurin:25-jdk AS construcao
WORKDIR /construcao
COPY gradlew settings.gradle build.gradle gradle.properties ./
COPY gradle gradle
RUN sh ./gradlew --no-daemon --quiet dependencies > /dev/null
COPY src/main src/main
RUN sh ./gradlew --no-daemon --quiet installDist && chmod -R a+rX build/install

FROM eclipse-temurin:25-jre
RUN useradd --system --no-create-home linketinder
WORKDIR /app
COPY --from=construcao /construcao/build/install/ms-competencias ./
USER linketinder
EXPOSE 8082
ENTRYPOINT ["./bin/ms-competencias"]
