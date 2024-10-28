#!/bin/bash

# Check if the current directory is named 'scripts'
ORIG_DIR="$PWD"
CURRENT_DIR=$(basename "$PWD")
if [ "$CURRENT_DIR" != "scripts" ]; then
    echo "This script must be run from the directory named 'scripts' in v4-native-ios/scripts"
    exit 1
fi

# Defining a temporary directory for cloning
TMP_DIR=$(mktemp -d)

# Function to clean up the temporary directory
cleanup() {
    echo "Cleaning up..."
    rm -rf "$TMP_DIR"
}

# Trap to clean up in case of script exit or interruption
trap cleanup EXIT

# Cloning into the temporary directory and navigating there
git clone git@github.com:dydxprotocol/v4-clients.git "$TMP_DIR/v4-clients"
cd "$TMP_DIR/v4-clients/v4-client-js"

# If cloning fails, exit the script
if [ $? -ne 0 ]; then
    echo "Failed to clone the v4-clients repository. Please check your network connection and repository access."
    exit 1
fi

source ~/.zprofile

# Running npm commands
nvm install
nvm use
npm install
npm run build
npm run webpack

# Check if the target directory for copying exists
TARGET_DIR="$ORIG_DIR/../v4/integration/cosmos/src/main/assets"
echo "$TARGET_DIR"
if [ -d "$TARGET_DIR" ]; then
    # Copying the file to the specified location
    cp __native__/__ios__/v4-native-client.js "$TARGET_DIR/v4-native-client.js"
else
    echo "Target directory $TARGET_DIR does not exist. File not copied."
fi

# Replace private functions and properties that start with #
${ORIG_DIR}/replace_hash_sign.sh "$TARGET_DIR/v4-native-client.js"
