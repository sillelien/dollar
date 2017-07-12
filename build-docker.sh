#!/usr/bin/env bash
set -eux
cd $(dirname $0)
DIR=$(pwd)
RELEASE=$(cat .release)
docker build -f Dockerfile-windows -t  sillelien-docker-docker.bintray.io/dollarscript-windows:${RELEASE:-dev}
docker build -f Dockerfile-headless -t  sillelien-docker-docker.bintray.io/dollarscript-headless:${RELEASE:-dev}

