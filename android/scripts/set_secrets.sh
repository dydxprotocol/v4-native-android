#!/bin/sh

mkdir -p ../v4/app/src/debug
cp secrets/google-services.json.debug ../v4/app/src/debug/google-services.json

mkdir -p ../v4/app/src/release
cp secrets/google-services.json.release ../v4/app/src/release/google-services.json

mkdir -p ../v4/common/src/main/res/values
cp secrets/strings.xml ../v4/common/src/main/res/values
