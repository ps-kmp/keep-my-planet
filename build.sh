#!/usr/bin/env bash
set -o errexit

cd KeepMyPlanet

chmod +x ./gradlew

./gradlew :server:installDist
