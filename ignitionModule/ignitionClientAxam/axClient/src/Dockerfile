# Use the official OpenJDK 17 image as the base image
FROM openjdk:17-alpine

# Set the working directory in the container
WORKDIR /app

# Copy the JAR files needed for compilation and testing into the container
COPY ./junit-jupiter-api-5.11.0-M2.jar ./apiguardian-api-1.1.2.jar ./junit-platform-console-standalone-1.8.2.jar /app/

# Copy the source code into the container
COPY ./util /app/util
COPY ./communication /app/communication
COPY ./test /app/test

# Create the output directory
RUN mkdir -p out

# Compile the util classes
RUN javac -d out util/*.java

# Compile the communication classes
RUN javac -d out -classpath out:./junit-jupiter-api-5.11.0-M2.jar:./apiguardian-api-1.1.2.jar communication/*.java

# Compile the test classes
RUN javac -d out -classpath out:./junit-jupiter-api-5.11.0-M2.jar:./apiguardian-api-1.1.2.jar test/CommunicationTestUtil.java
RUN javac -d out -classpath out:./junit-jupiter-api-5.11.0-M2.jar:./apiguardian-api-1.1.2.jar test/apiTest/ApiTest.java

# Create a manifest file for the JAR
RUN echo "Main-Class: org.junit.platform.console.ConsoleLauncher" > out/manifest.txt

# Package the compiled files into a JAR file
RUN jar cfm clientProj.jar out/manifest.txt -C out .

# Clean up the manifest file
RUN rm out/manifest.txt

# Command to run the JUnit tests using the built JAR file
CMD ["java", "-cp", "clientProj.jar:./junit-jupiter-api-5.11.0-M2.jar:./apiguardian-api-1.1.2.jar:./junit-platform-console-standalone-1.8.2.jar", "test/apiTest/ApiTest"]

