#!/bin/sh

json=$(gh api \
  -H "Accept: application/vnd.github+json" \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  /orgs/dydxprotocol/packages/maven/dydxprotocol.cartera-android/versions)

NEW_VERSION=$(echo $json | jq -r '.[0].name')

targetFileName="v4/build.gradle"
OLD_VERSION=$(grep "^    carteraVersion = " $targetFileName | sed -n 's/    carteraVersion = ''\(.*\)''/\1/p')

if [ -n "$NEW_VERSION" ] && [ -n "$OLD_VERSION" ]; then 
  echo "Bumping Cartera version from $OLD_VERSION to $NEW_VERSION"
  sed -i '' "s/^    carteraVersion = $OLD_VERSION/    carteraVersion = '$NEW_VERSION'/" $targetFileName 
  echo "Version bumped to $NEW_VERSION"
else
  echo "No version found"
fi

