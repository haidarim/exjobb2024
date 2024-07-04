#!/bin/bash

# Function to perform only the build
only_build() {
    dotnet build
    echo "-----------------------> Build completed."
}

# Function to perform the build and grep the output
build_and_grep() {
    # Capture the build output
    build_output=$(dotnet build)

    # Print the build output
    #echo "$build_output"

    # Search for a specific pattern in the build output
    echo "$build_output" | grep "$1"
}

start(){
    dotnet run ./bin/Debug/net8.0/ServerSim.dll
    
}

remove(){
    sudo rm -r bin
    sudo rm -r obj
    rm -r dump.rdb
}

# Check command-line arguments
if [ $# -eq 0 ]; then
    # No arguments provided, perform only the build
    only_build
elif [ "$1" == "start" ]; then
    start
elif [ "$1" == "build_and_grep" ]; then
    if [ -z "$2" ]; then
        echo "Usage: $0 build_and_grep <pattern>"
        exit 1
    fi
    # Perform build and grep with the provided pattern
    build_and_grep "$2"
elif [ "$1" == "remove" ]; then
    remove
else
    echo "Usage: $0 {build|build_and_grep <pattern>}"
    exit 1
fi
