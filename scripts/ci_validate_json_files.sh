#!/bin/sh

find . -name "*.json" -print0 | while IFS= read -r -d '' file; do
    echo "Validating $file"
    if ! python -m json.tool "$file" > /dev/null 2>&1; then
        echo "Invalid JSON file: '$file'"
        exit 1
    fi
done
