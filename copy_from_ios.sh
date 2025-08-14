#!/bin/sh

for dir in rn_style TurnkeyReact App.tsx eslint.config.mjs Gemfile Gemfile.lock global.css index.js metro.config.js package-lock.json package.json react-native.config.js svg.d.ts tsconfig.json TurnkeyCallbackProvider.tsx TurnkeyModule.ts yarn.lock; do
  cp -r ../v4-native-ios/$dir .
done