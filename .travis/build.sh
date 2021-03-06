#!/bin/bash

git fetch --unshallow #required for commit count

if [ -z "$TRAVIS_TAG" ]; then
    ./gradlew clean assembleDebug

    COMMIT_COUNT=$(git rev-list --count HEAD)
    export ARTIFACT="kitvei-r${COMMIT_COUNT}.apk"

    if [ -f app/build/outputs/apk/debug/app-debug.apk ]; then
        mv app/build/outputs/apk/debug/app-debug.apk $ARTIFACT
    fi
else
    ./gradlew clean assembleRelease

    TOOLS="$(ls -d ${ANDROID_HOME}/build-tools/* | tail -1)"
    export ARTIFACT="kitvei-${TRAVIS_TAG}.apk"

    ${TOOLS}/zipalign -v -p 4 app/build/outputs/apk/release/app-release-unsigned.apk app-aligned.apk
    ${TOOLS}/apksigner sign --ks $STORE_PATH --ks-key-alias $STORE_ALIAS --ks-pass env:STORE_PASS --key-pass env:KEY_PASS --out $ARTIFACT app-aligned.apk
fi