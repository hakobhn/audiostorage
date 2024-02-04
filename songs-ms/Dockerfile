FROM maven:3.6.0-jdk-11-slim AS build
COPY src /usr/app/src
COPY pom.xml /usr/app
RUN mvn -f /usr/app/pom.xml clean package


FROM openjdk:11-jre-slim
COPY --from=build /usr/app/target/songs-*.jar /usr/deploy/app.jar
EXPOSE 8081
CMD ["java","-jar","/usr/deploy/app.jar"]