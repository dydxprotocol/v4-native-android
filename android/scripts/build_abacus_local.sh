#!/bin/sh

# This script is used to build the abacus project locally.
#
# Please run this after making local change to the abacus project.  The change will be
# picked up by the v4-native-android project (via the local maven repository).  Note that
# the script needs to be run everytime a change is made to the abacus project.
#
# The script will update both the v4-abacus and v4-native-android repositories, so please make sure
# you clean-up/revert the changes after you are done with the local testing

ANDROID_DIR=$(pwd)
ABACUS_DIR=$(pwd)/../v4-abacus

# Create a random version number
NEW_VERSION="local.$(date +%s)"

# search for the first line that starts with "version" in build.gradle.kts
# get the value in the quotes
VERSION=$(grep "^version = " ${ABACUS_DIR}/build.gradle.kts | sed -n 's/version = "\(.*\)"/\1/p')

sed -i '' "s/version = \"$VERSION\"/version = \"$NEW_VERSION\"/" ${ABACUS_DIR}/build.gradle.kts
echo "Version bumped to $NEW_VERSION"

echo "Building Abacus ..."

cd ${ABACUS_DIR}
./gradlew publishToMavenLocal

cd ${ANDROID_DIR}

# get the version from the file
targetFileName="build.gradle"
OLD_VERSION=$(grep "^    abacusVersion = " $targetFileName | sed -n 's/    abacusVersion = ''\(.*\)''/\1/p')

if [ -n "$NEW_VERSION" ] && [ -n "$OLD_VERSION" ]; then
  echo "Bumping Abacus version from $OLD_VERSION to $NEW_VERSION"
  sed -i '' "s/^    abacusVersion = $OLD_VERSION/    abacusVersion = '$NEW_VERSION'/" $targetFileName
  echo "Version bumped to $NEW_VERSION"
else
  echo "No version found"
fi
