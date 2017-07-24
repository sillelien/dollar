#!/bin/bash -ex
set -eux
cd $(dirname $0) && cd ..
PROJECT=$(pwd)
RELEASE=$(cat .release)

#Bintray
#docker login -u neilellis -p ${BINTRAY_API_KEY} -e hello@sillelien.com sillelien-docker-docker.bintray.io
#docker push sillelien-docker-docker.bintray.io/dollarscript-headless:${RELEASE:-dev}
#docker push sillelien-docker-docker.bintray.io/dollarscript-headless:${MAJOR_VERSION}

#Docker Hub
docker login -u $DOCKER_USER -p $DOCKER_PASS
docker push sillelien/dollarscript-headless:${RELEASE:-dev}
docker push sillelien/dollarscript-headless:${MAJOR_VERSION:-dev}
