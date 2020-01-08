FROM openjdk:8-alpine AS build
RUN apk add bash
WORKDIR /bsbm
COPY . .
RUN bash ./gradlew installDist

FROM openjdk:8-jre-alpine
RUN apk add bash --no-cache
WORKDIR /bsbm
COPY --from=build /bsbm/build/install/bsbm .
