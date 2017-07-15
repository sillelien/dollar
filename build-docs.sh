#!/bin/bash -eux
cd $(dirname $0)
DIR=$(pwd)
./create-github-file.sh
git clone https://github.com/sillelien/dollar project
git checkout $CIRCLE_BRANCH
mkdir -p dist
cd dist
git clone https://github.com/sillelien/dollar docs
cd docs
git checkout gh-pages
ls /root/dollar/project/dollar-docs/src/main/webapp/
jekyll build --source $DIR/project/dollar-docs/src/main/webapp/ --destination $DIR/dist/docs
cp -rf * $DIR/dist/docs || :
