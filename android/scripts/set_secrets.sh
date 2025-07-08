#!/bin/sh

mkdir -p ../app/src/debug
cp secrets/google-services.json.debug ../app/src/debug/google-services.json

mkdir -p ../app/src/release
cp secrets/google-services.json.release ../app/src/release/google-services.json

mkdir -p ../v4/common/src/main/res/values
cp secrets/strings.xml ../v4/common/src/main/res/values
