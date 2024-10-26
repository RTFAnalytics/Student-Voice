FROM openjdk:17
WORKDIR /app
COPY ./target/student-voice-0.2.jar /app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "student-voice-0.2.jar"]