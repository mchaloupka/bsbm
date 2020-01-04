FROM openjdk:8-alpine
RUN apk add --no-cache bash
WORKDIR /bsbm
COPY . .
RUN bash ./gradlew installDist
WORKDIR /bsbm/build/install/bsbm
