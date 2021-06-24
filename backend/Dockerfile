FROM maven:3.6.3-jdk-13 AS build

WORKDIR /server
COPY mvnw .
COPY .mvn .mvn

COPY pom.xml .

COPY ./src/ ./src/



RUN mvn package -Dmaven.test.skip=true



FROM openjdk:13-jdk-alpine

WORKDIR /server


COPY --from=build /server/target/backend-0.0.1-SNAPSHOT.jar server.jar
COPY --from=build /server/src ./src/


RUN ["apk", "add","git"]
RUN ["apk", "add","curl"]

ENTRYPOINT ["java","-Dspring.data.mongodb.uri=mongodb://mymongodb:27017/github_db","-jar","server.jar"]
