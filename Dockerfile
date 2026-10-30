FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY . .
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
RUN useradd --system --create-home --uid 10001 jaya \
    && mkdir -p /data \
    && chown jaya:jaya /data
WORKDIR /opt/jaya
COPY --from=build /src/jaya-app/target/jaya-app.jar app.jar
COPY --from=build /src/jaya-app/target/lib lib
USER jaya
EXPOSE 8080
ENV JAYA_HOST=0.0.0.0
ENV JAYA_PORT=8080
ENV JAYA_DATA_DIR=/data
ENV JAYA_ENV=local
ENV JAYA_DEV_TOKENS=true
CMD ["java", "-jar", "app.jar"]
