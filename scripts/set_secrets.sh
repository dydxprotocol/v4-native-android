#!/bin/sh

cp secrets/google-services.json.debug ../v4/app/src/debug/google-services.json
cp secrets/google-services.json.release ../v4/app/src/release/google-services.json
cp secrets/strings.xml ../v4/common/src/main/res/values
