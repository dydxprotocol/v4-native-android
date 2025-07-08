#!/bin/sh

# Define the repository and file information
repository="dydxprotocol/v4-abacus"
filename="build.gradle.kts"
branch="main" # Replace with the branch you want to use

# Define the GitHub API URL to retrieve file information
api_url="https://api.github.com/repos/$repository/contents/$filename?ref=$branch"

# Use curl to fetch the file metadata from the GitHub API
file_info=$(curl -s "$api_url")

# Check if the file exists
if [[ "$file_info" == *"Not Found"* ]]; then
    echo "File $filename not found in $repository on branch $branch."
    exit 1
fi

# Extract the download URL for the file
download_url=$(echo "$file_info" | grep -o '"download_url": "[^"]*' | cut -d '"' -f 4)

targetFileName="build.gradle"
tmpFileName="/tmp/abacusGradle"

rm -rf $tmpFileName
curl -s -L "$download_url" > $tmpFileName

# get the version from the file

OLD_VERSION=$(grep "^    abacusVersion = " $targetFileName | sed -n 's/    abacusVersion = ''\(.*\)''/\1/p')

NEW_VERSION=$(grep "^version = " $tmpFileName | sed -n 's/version = "\(.*\)"/\1/p')

if [ -n "$NEW_VERSION" ] && [ -n "$OLD_VERSION" ]; then 
  echo "Bumping Abacus version from $OLD_VERSION to $NEW_VERSION"
  sed -i '' "s/^    abacusVersion = $OLD_VERSION/    abacusVersion = '$NEW_VERSION'/" $targetFileName 
  echo "Version bumped to $NEW_VERSION"
else
  echo "No version found"
fi




