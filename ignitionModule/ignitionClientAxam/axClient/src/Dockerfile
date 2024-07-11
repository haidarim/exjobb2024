# Use a base image with Java 17
FROM openjdk:17-slim

# Set working directory
WORKDIR /app

# Copy necessary files into the Docker image
COPY junit-jupiter-api-5.11.0-M2.jar .
COPY apiguardian-api-1.1.2.jar .
COPY junit-platform-console-standalone-1.8.2.jar .
COPY util /app/util
COPY communication /app/communication
COPY test /app/test

# Build the project into a JAR file
RUN mkdir -p out && \
    echo "---------------------> compiling util/*.java <------------------- " && \
    javac -d out util/*.java && \
    echo "---------------------> compiling communication/*.java <------------------- " && \
    javac -d out -classpath out:junit-jupiter-api-5.11.0-M2.jar:apiguardian-api-1.1.2.jar communication/*.java && \
    echo "---------------------> compiling test classes <------------------- " && \
    javac -d out -classpath out:junit-jupiter-api-5.11.0-M2.jar:apiguardian-api-1.1.2.jar test/CommunicationTestUtil.java && \
    javac -d out -classpath out:junit-jupiter-api-5.11.0-M2.jar:apiguardian-api-1.1.2.jar test/apiTest/ApiTest.java && \
    echo "Compilation successful. Proceeding to package JAR." && \
    echo "Main-Class: org.junit.platform.console.ConsoleLauncher" > out/manifest.txt && \
    echo "---------------------> packaging into JAR <------------------- " && \
    jar cfm clientProj.jar out/manifest.txt -C out . && \
    echo "---------------------> removing manifest <------------------- " && \
    rm out/manifest.txt

# Define the command to run tests
CMD ["java", "-cp", "clientProj.jar:junit-jupiter-api-5.11.0-M2.jar:apiguardian-api-1.1.2.jar:junit-platform-console-standalone-1.8.2.jar", "test/apiTest/ApiTest"]

