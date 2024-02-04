FROM maven:3.6.0-jdk-11-slim AS build
COPY src /usr/app/src
RUN rm -r /usr/app/src/test/java/com/epam/training/microservices/audio/resource_processor/it
COPY pom.xml /usr/app
RUN mvn -f /usr/app/pom.xml clean package


FROM openjdk:11-jre-slim
COPY --from=build /usr/app/target/resource-processor-*.jar /usr/deploy/app.jar
CMD ["java","-jar","/usr/deploy/app.jar"]