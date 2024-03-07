<div align="center">
  <img src='https://github.com/dydxprotocol/v4-native-android/blob/develop/meta/icon_512.png' alt='icon' style="width:180px;height:180px;"> />
</div>
<h1 align="center">v4-native-android</h1>

<div align="center">
  <a href='https://github.com/dydxprotocol/v4-native-android/blob/main/LICENSE'>
    <img src='https://img.shields.io/badge/License-AGPL_v3-blue.svg' alt='License' />
  </a>
</div>

This is the native Android app for dYdX v4.

# Quick Setup

```zsh
cd scripts
./bootstrap.sh
```

This will set up Android Studio project along with all dependencies.

# Main Repo

```zsh
git clone git@github.com:dydxprotocol/v4-native-android.git
```

# Repo Dependencies

### v4-abacus and cartera-android

This project requires the latest packages from v4-abacus and cartera-android.

To access v4-abacus and cartera-android, create a personal access token on Github and add it to the repo [link](https://docs.github.com/en/enterprise-server@3.4/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens).  Create a classic token that has read permission to packages.

Add your github user name and token as shell env vars:
```zsh
export github_username=[GITHUB_USERNAME]
export github_token=[GITHUB_TOKEN]
```

<img width="807" alt="281211719-e0af477e-f84c-466f-93ac-32d0236bb84b" src="https://github.com/dydxprotocol/v4-native-android/assets/102453770/46b7b613-b194-4f5f-8b5a-aadff548f234">


### v4-localization and v4-web

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

```zsh
cd v4-native-android/scripts
./set_secrets.sh
```

This will apply the secrets to the source code.

# Build Android App

```zsh
cd v4-native-android
./gradlew build
./gradlew :v4:app:installDebug
```

### To check for unused or missing dependencies:
`./gradlew buildHealth`

### To clean up many lint errors automatically:
`./gradlew spotlessApply`




