# Alpine Linux with OpenJDK JRE
FROM openjdk:8-jre-alpine
RUN apk add --no-cache bash
WORKDIR /bsbm
COPY . .
RUN bash ./gradlew
