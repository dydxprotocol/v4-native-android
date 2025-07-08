#!/bin/sh

buildGradle="app/build.gradle"

# Get the current version
version=$(grep "versionName " $buildGradle | cut -d "\"" -f2)
echo "Current version: $version"
echo "Enter new version: "
read newVersion

# Replace the version
sed -i '' "s/versionName \"$version\"/versionName \"$newVersion\"/g" $buildGradle

