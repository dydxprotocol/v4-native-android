#!/bin/sh

# This script replaces the private function and property names in a file with a new name
# so that Android devices with browsers that supports old javascript syntax can run the code

# Check if filename is provided as argument
if [ $# -ne 1 ]; then
    echo "Usage: $0 <filename>"
    exit 1
fi

filename="$1"

# Check if the file exists
if [ ! -f "$filename" ]; then
    echo "Error: File '$filename' not found."
    exit 1
fi

# create an empty array to store the names
functionNames=()
propertyNames=()

# Read the file line by line and print each line
while IFS= read -r line; do
    # "    #name(....) {"
    if [[ $line =~ ^\ \ \ \ \#.*\{$ ]]; then
        # extract the string beween # and (, and remove the rest
        name=`echo "${line#*#}" | sed 's/(.*//'`
        # add the name to the names array
        functionNames+=("$name")
    fi

    # "    async #name(....) {"
    if [[ $line =~ ^\ \ \ \ \async\ #.*\{$ ]]; then
        # extract the string beween # and (, and remove the rest
        name=`echo "${line#*#}" | sed 's/(.*//'`
        # add the name to the names array
        functionNames+=("$name")
    fi

    # "    static async #name(....) {"
    if [[ $line =~ ^\ \ \ \ \static\ async\ #.*\{$ ]]; then
        # extract the string beween # and (, and remove the rest
        name=`echo "${line#*#}" | sed 's/(.*//'`
        # add the name to the names array
        functionNames+=("$name")
    fi

    if [[ $line =~ ^\ \ \ \ \static\ #.*\{$ ]]; then
        # extract the string beween # and (, and remove the rest
        name=`echo "${line#*#}" | sed 's/(.*//'`
        # add the name to the names array
        functionNames+=("$name")
    fi

    if [[ $line =~ ^\ \ \ \ \#.*\;$ ]]; then
        # extract the string beween # and ;, and remove the rest
        name=`echo "${line#*#}" | sed 's/;.*//'`
        propertyNames+=("$name")
    fi
done < "$filename"

# remove duplicates from the array
functionNames=($(echo "${functionNames[@]}" | tr ' ' '\n' | sort -u | tr '\n' ' '))
propertyNames=($(echo "${propertyNames[@]}" | tr ' ' '\n' | sort -u | tr '\n' ' '))

# replace the names in the file
for name in "${functionNames[@]}"; do
    echo "Replacing function name: #$name"
    before="#$name("
    after="___$name("
    # replace the before string with the after string in the file
    sed -i '' "s/$before/$after/g" $filename
done

for name in "${propertyNames[@]}"; do
    echo "Replacing property name: #$name"
    before="\ #$name"
    after="\ ___$name"
    sed -i '' "s/$before/$after/g" $filename
    before="\.#$name"
    after="\.___$name"
    sed -i '' "s/$before/$after/g" $filename
done