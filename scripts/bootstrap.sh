#!/bin/sh

ROOT_DIR=$(pwd)/../../

# Install Homebrew
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Xcode
ANDROID_STUDIO=/Applications/Android\ Studio.app
if [ ! -d "$ANDROID_STUDIO" ]; then
	brew install --cask android-studio
fi

brew install cocoapods
brew install java
brew install gradle
brew install gh

cd "$ROOT_DIR"

if [ ! -d "v4-localization" ]; then
	git clone git@github.com:dydxprotocol/v4-localization.git
else
	cd v4-localization
	git pull
	cd ..
fi

if [ ! -d "v4-web" ]; then
	git clone git@github.com:dydxprotocol/v4-web.git
else
	cd v4-web
	git pull
	cd ..
fi

