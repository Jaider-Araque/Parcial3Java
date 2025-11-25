FROM openjdk:21
COPY "./target/saber-pro-system-0.0.1-SNAPSHOT.jar" "app.jar"
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]