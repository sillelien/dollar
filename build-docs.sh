#!/bin/bash -eux
cd $(dirname $0)
DIR=$(pwd)
echo "login=neilellis" > ~/.github
echo "password=${GITHUB_PASSWORD}" >> ~/.github
git clone https://github.com/sillelien/dollar project
git checkout $CIRCLE_BRANCH
mkdir -p dist
cd dist
git clone https://github.com/sillelien/dollar docs
cd docs
git checkout gh-pages
jekyll build --source $DIR/project/dollar-docs/src/main/webapp/ --destination .
cp -rf * $DIR/dist/docs || :
