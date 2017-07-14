#!/usr/bin/env bash
set -eux
cd $(dirname $0)
DIR=$(pwd)
RELEASE=$(cat .release)
docker build -f $DIR/Dockerfile-windows -t  sillelien-docker-docker.bintray.io/dollarscript-windows:${RELEASE:-dev}
docker build -f $DIR/Dockerfile-headless -t  sillelien-docker-docker.bintray.io/dollarscript-headless:${RELEASE:-dev}

