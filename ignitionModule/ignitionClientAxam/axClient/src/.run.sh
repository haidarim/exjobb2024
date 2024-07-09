#!/bin/bash

# Path to the JUnit Jupiter API JAR file
JUNIT_API_JAR=./junit-jupiter-api-5.11.0-M2.jar
# Path to the Apiguardian API JAR file
APIGUARDIAN_API_JAR=./apiguardian-api-1.1.2.jar
# Path to the JUnit Platform Console Standalone JAR file
JUNIT_PLATFORM_CONSOLE_JAR=./junit-platform-console-standalone-1.8.2.jar

# Function to remove the output directory
remove() {
    sudo rm -r out
    sudo rm -r clientProj.jar
}

# Function to compile and package the project into a JAR file
takeJar() {
    # Create the output directory if it doesn't exist
    mkdir -p out

    echo "---------------------> compiling util/*.java <------------------- "
    javac -d out  util/*.java


    echo "---------------------> compiling communication/*.java <------------------- "
    javac -d out -classpath out:"$JUNIT_API_JAR":"$APIGUARDIAN_API_JAR" communication/*.java

    # Compile test classes last, including JUnit in the classpath
    echo "---------------------> compiling test classes <------------------- "
    javac -d out -classpath out:"$JUNIT_API_JAR":"$APIGUARDIAN_API_JAR" test/CommunicationTestUtil.java
    javac -d out -classpath out:"$JUNIT_API_JAR":"$APIGUARDIAN_API_JAR" test/apiTest/ApiTest.java

    # Check compilation status
    if [ $? -eq 0 ]; then
        echo "Compilation successful. Proceeding to package JAR."
        # Create a manifest file for the JAR
        echo "Main-Class: org.junit.platform.console.ConsoleLauncher" > out/manifest.txt

        # Package the compiled files into a JAR file
        echo "---------------------> packaging into JAR <------------------- "
        jar cfm clientProj.jar out/manifest.txt -C out .

        # Clean up the manifest file
        echo "---------------------> removing manifest <------------------- "
        rm out/manifest.txt
    else
        echo "Compilation failed. Please check the errors and try again."
    fi
}

# Function to run JUnit tests using the built JAR file
runTests() {
    echo "Running JUnit tests using clientProj.jar" 
    java -cp clientProj.jar:"$JUNIT_API_JAR":"$APIGUARDIAN_API_JAR":"$JUNIT_PLATFORM_CONSOLE_JAR" test/apiTest/ApiTest
}

# Check command-line arguments
if [ $# -eq 0 ]; then
    # No arguments provided, perform only the build and run tests
    remove    
    takeJar
    runTests
elif [ "$1" == "remove" ]; then
    remove
else
    echo "Usage: $0 {build|remove}"
    exit 1
fi

