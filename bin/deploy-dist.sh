#!/bin/bash -eux
cd $(dirname $0) && cd ..
PROJECT=$(pwd)
export BINTRAY_USER=neilellis
export BINTRAY_KEY=${BINTRAY_API_KEY}
mkdir -p $HOME/workspace/artifacts/
VERSION=$(cat .release)
. /home/circleci/build-utils/bintray-functions.sh
mv  $HOME/workspace/dist/dollar.tgz dist/dollar-${VERSION}.tgz
bint-upload-with-version sillelien binary dollar ${VERSION}  $HOME/workspace/artifacts/dollar-${VERSION}.tgz

export AWS_SECRET_ACCESS_KEY=$AWS_SECRET_KEY
aws configure list
aws s3 cp  $HOME/workspace/artifacts/dollar-${VERSION}.tgz s3://dollarscript/dist/dollar-${VERSION}.tgz
