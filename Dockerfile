ARG BUILD_IMAGE=maven:3.5-jdk-11
ARG RUNTIME_IMAGE=openjdk:11

#############################################################################################
###                Stage where Docker is pulling all maven dependencies                   ###
#############################################################################################
FROM ${BUILD_IMAGE} as dependencies

COPY pom.xml ./pom.xml

RUN mvn -B dependency:go-offline

#############################################################################################
###              Stage where Docker is building spring boot app using maven               ###
#############################################################################################
FROM dependencies as build

COPY ./src ./src

RUN mvn clean package

#############################################################################################
### Stage where Docker is running a java process to run a service built in previous stage ###
#############################################################################################
FROM ${RUNTIME_IMAGE}

ENV POSTGRES_USER "$POSTGRES_USER"
ENV POSTGRES_PASSWORD "$POSTGRES_PASSWORD"
ENV POSTGRES_DB "$POSTGRES_DB"

COPY --from=build ./target/qroom-backend-*.jar service.jar

CMD ["java", "-jar", "service.jar"]
#############################################################################################