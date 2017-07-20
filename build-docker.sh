#!/usr/bin/env bash
set -eux
cd $(dirname $0)
PROJECT=$(pwd)
RELEASE=$(cat .release)
cd dist
docker build -f ${PROJECT}/Dockerfile-windows -t  sillelien-docker-docker.bintray.io/dollarscript-windows:${RELEASE:-dev} .
docker build -f ${PROJECT}/Dockerfile-headless -t  sillelien-docker-docker.bintray.io/dollarscript-headless:${RELEASE:-dev} .

