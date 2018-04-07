#!/bin/sh -ex

# Publish build artifacts to Maven
./gradlew publish -Pgit.commit=$TRAVIS_COMMIT
