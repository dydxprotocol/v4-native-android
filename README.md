# Background

This is the native Android app for dYdX

# Quick Setup

> cd scripts

> ./bootstrap.sh

This will set up Android Studio project along with all dependencies.

# Main Repo

git clone git@github.com:dydxprotocol/v4-native-android.git

# Repo Dependencies

This project requires v4-localization

https://github.com/dydxprotocol/v4-localization

This project requires v4-web

https://github.com/dydxprotocol/v4-web

The Android Studio project expects those two repos to be cloned side-by-side to the main Android repo.

Other dependencies are specified in the Android project's `build.gradle` as expected.

# API Keys & Secrets

Unzip the `secrets.zip` from the `Android Secrets` vault in the dYdX 1Password account. Ask a team member for access.
Add the `secrets/` folder to the `v4-native-android/scripts` folder.

Copy the content of `secrets.zip` to `v4-native-android/scripts/secrets` and run

cd v4-native-android/scripts
./set_secrets.sh

This will apply the secrets to the source code.

# Build Android App

cd v4-native-android
./gradlew build
./gradlew :v4:app:installDebug
```

### To check for unused or missing dependencies:
`./gradlew buildHealth`

### To clean up many lint errors automatically:
`./gradlew spotlessApply`




