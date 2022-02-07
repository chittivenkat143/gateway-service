FROM adoptopenjdk/openjdk11 as builder
EXPOSE 8081
WORKDIR gateway-service
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} gateway-service.jar
RUN java -Djarmode=layertools -jar gateway-service.jar extract

FROM adoptopenjdk/openjdk11
WORKDIR gateway-service
COPY --from=builder gateway-service/dependencies/ ./
COPY --from=builder gateway-service/spring-boot-loader/ ./
COPY --from=builder gateway-service/snapshot-dependencies/ ./
COPY --from=builder gateway-service/application/ ./
ENTRYPOINT [ "java", "org.springframework.boot.loader.JarLauncher" ]