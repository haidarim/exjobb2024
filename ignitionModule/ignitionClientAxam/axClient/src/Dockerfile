# Use a base image with JDK for building
FROM openjdk:15 AS build

# Set the working directory
WORKDIR /app

# Copy the already compiled .class files into the container
COPY ./out /app/out

# Create a JAR file containing all .class files
RUN jar cvf app.jar -C /app/out .

# Second stage: Run the application
FROM openjdk:15-slim

# Set the working directory
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/app.jar /app/app.jar


# Command to run each class from the JAR and print output
CMD ["java", "-cp", "/app/out/ignitionClientAXAM/test/apiTest/ApiTest", "test.apiTest.ApiTest"]
