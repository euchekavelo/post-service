FROM openjdk:17
RUN mkdir /app
COPY ./build/libs/*.jar /app/app-post-service.jar
EXPOSE 8081
WORKDIR /app
CMD java -jar app-post-service.jar