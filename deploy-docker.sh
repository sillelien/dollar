#!/bin/bash -ex
set -eux
cd $(dirname $0)
DIR=$(pwd)
RELEASE=$(cat .release)
docker login -u neilellis -p ${BINTRAY_API_KEY} -e hello@sillelien.com sillelien-docker-docker.bintray.io
docker push sillelien-docker-docker.bintray.io/dollarscript-windows:${RELEASE:-dev}
docker push sillelien-docker-docker.bintray.io/dollarscript-headless:${RELEASE:-dev}
