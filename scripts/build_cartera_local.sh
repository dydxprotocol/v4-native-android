#!/bin/sh

# This script is used to build the cartera project locally.
#
# Please run this after making local change to the cartera project.  The change will be
# picked up by the v4-native-android project (via the local maven repository).  Note that
# the script needs to be run everytime a change is made to the cartera project.
#
# The script will update both the cartera-android and v4-native-android repositories, so please make sure
# you clean-up/revert the changes after you are done with the local testing

CARTERA_DIR=~/cartera-android
ANDROID_DIR=~/v4-native-android

# Create a random version number
NEW_VERSION="local.$(date +%s)"

cd ${CARTERA_DIR}

# search for the first line that starts with "version" in build.gradle.kts
# get the value in the quotes
VERSION=$(grep "^LIBRARY_VERSION_NAME=" gradle.properties | sed -n 's/LIBRARY_VERSION_NAME=\(.*\)/\1/p')

sed -i '' "s/LIBRARY_VERSION_NAME=$VERSION/LIBRARY_VERSION_NAME=$NEW_VERSION/" gradle.properties
echo "Version bumped to $NEW_VERSION"

echo "Building Cartera ..."

./gradlew publishToMavenLocal

cd ${ANDROID_DIR}

# get the version from the file
targetFileName="v4/build.gradle"
OLD_VERSION=$(grep "^    carteraVersion = " $targetFileName | sed -n 's/    carteraVersion = ''\(.*\)''/\1/p')

if [ -n "$NEW_VERSION" ] && [ -n "$OLD_VERSION" ]; then
  echo "Bumping Cartera version from $OLD_VERSION to $NEW_VERSION"
  sed -i '' "s/^    carteraVersion = $OLD_VERSION/    carteraVersion = '$NEW_VERSION'/" $targetFileName
  echo "Version bumped to $NEW_VERSION"
else
  echo "No version found"
fi
