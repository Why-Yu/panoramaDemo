FROM openjdk:11
VOLUME /tmp
ADD ./target/panoramaDemo-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/urandom", "-jar", "/app.jar"]