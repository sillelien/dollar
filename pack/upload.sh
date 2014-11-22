#!/bin/bash -eu
cd $(dirname $0)
export BINTRAY_USER=neilellis
export BINTRAY_KEY=${BINTRAY_API_KEY}

curl -Lo /tmp/bintray-functions j.mp/bintray-functions && . /tmp/bintray-functions

mv ../dist/osx/dollar.tgz ../dist/osx/dollar-${1}.tgz
bint-upload-with-version neilellis dollar dollar-runtime-osx ${1} ../dist/osx/dollar-${1}.tgz
