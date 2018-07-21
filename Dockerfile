FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/config-server-example-0.0.1-SNAPSHOT-standalone.jar /config-server-example/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/config-server-example/app.jar"]
