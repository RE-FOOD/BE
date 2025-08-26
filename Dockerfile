FROM openjdk:21-jdk
ENV APP_HOME=/home/ubuntu/BE
WORKDIR $APP_HOME
COPY build/libs/refood-server.jar refood-server.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=dev","refood-server.jar"]