# Alpine Linux with OpenJDK JRE
FROM openjdk:8-jre-alpine
RUN apk add --no-cache bash
RUN apk add --no-cache dos2unix
WORKDIR /bsbm
COPY . .
RUN dos2unix ./gradlew
RUN bash ./gradlew
