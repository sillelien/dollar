#!/usr/bin/env bash
set -eux
cd $(dirname $0) && cd ..
PROJECT=$(pwd)
RELEASE=$(cat .release)

mkdir -p $PROJECT/dist/

cd $PROJECT/dist
curl http://dollarscript.s3-website-eu-west-1.amazonaws.com/dist/dollar-${RELEASE}.tgz > dollar.tgz
tar -zxvf dollar.tgz
ls -l dollar

cp ${PROJECT}/docker/Dockerfile-windows .
cp ${PROJECT}/docker/Dockerfile-headless .

#Headless
docker build -f Dockerfile-headless -t sillelien-docker-docker.bintray.io/dollarscript-headless:${RELEASE:-dev} .
docker build -f Dockerfile-headless -t sillelien-docker-docker.bintray.io/dollarscript-headless:${MAJOR_VERSION:-dev} .
docker build -f Dockerfile-headless -t sillelien/dollarscript-headless:${RELEASE:-dev} .
docker build -f Dockerfile-headless -t sillelien/dollarscript-headless:${MAJOR_VERSION:-dev} .

