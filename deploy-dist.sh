#!/bin/bash -eux
cd $(dirname $0)
export BINTRAY_USER=neilellis
export BINTRAY_KEY=${BINTRAY_API_KEY}
VERSION=$(cat .release)

curl -Lo /tmp/bintray-functions j.mp/bintray-functions && . /tmp/bintray-functions

mv dist/dollar.tgz dist/dollar-${VERSION}.tgz
bint-upload-with-version sillelien binary dollar ${VERSION} dist/dollar-${VERSION}.tgz
