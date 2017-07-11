#!/bin/bash -eux
cd $(dirname $0)
export BINTRAY_USER=neilellis
export BINTRAY_KEY=${BINTRAY_API_KEY}

curl -Lo /tmp/bintray-functions j.mp/bintray-functions && . /tmp/bintray-functions

mv ../dist/dollar.tgz ../dist/dollar-${1}-${2}.tgz
bint-upload-with-version sillelien binary dollar ${1} ../dist/dollar-${1}-${2}.tgz
