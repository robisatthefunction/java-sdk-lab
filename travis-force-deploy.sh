#!/usr/bin/env bash
set -e

# always run tests
./gradlew --no-daemon --max-workers 1 check

# Travis merges the PR into master so we need to extract the originating commit.
MERGE_REGEX='Merge (.*) into .*'

# If this is a travis merge commit and the commit message was PUBLISH, then
# additionally publish the artifacts. Note that the published artifacts
# will include the PR and build number as part of the version.
# See gradle/publish.gradle
if [[ $TRAVIS_COMMIT_MESSAGE =~ $MERGE_REGEX ]]; then
    TRIGGER_COMMIT=${BASH_REMATCH[1]}
    COMMIT_MESSAGE=`git log --format=%B -n 1 "$TRIGGER_COMMIT"`

    if [[ $COMMIT_MESSAGE == "PUBLISH" ]]; then
      echo "Running publish."
      ./gradlew publish -Pgit.commit=$TRAVIS_COMMIT
    fi
fi
