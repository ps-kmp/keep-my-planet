#!/usr/bin/env bash
set -o errexit

chmod +x ./gradlew

./gradlew :server:installDist