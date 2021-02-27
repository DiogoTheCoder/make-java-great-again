#!/bin/bash

# Clear out the old compiled TS files
rm -rf out/

# Run tsc -p ./
yarn run compile

# Copy over the syntaxes folder (JSON files) to the out
cp -a src/syntaxes out/src/syntaxes

# Copy over the App.java used for testing
mkdir -p "out/src/test/suite/java"
cp -r src/test/suite/java/. out/src/test/suite/java

# And run the tests!
node ./out/src/test/runTest.js