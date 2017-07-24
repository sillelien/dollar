#!/bin/bash -eux
cd $(dirname $0)
DIR=$(pwd)

./create-github-file.sh
./set-version.sh

git clone https://github.com/sillelien/dollar project
git checkout $CIRCLE_BRANCH
mkdir -p dist
cd dist
git clone https://github.com/sillelien/dollar docs
cd docs
git checkout gh-pages

ls /root/dollar/project/dollar-docs/src/main/webapp/
envsubst < $DIR/project/dollar-docs/src/main/webapp/_env.yml > $DIR/project/dollar-docs/src/main/webapp/env.yml
tail -n +2 $DIR/project/dollar-docs/src/main/resources/pages/scripting.md  > $DIR/project/dollar-docs/src/main/webapp/scripting.md
jekyll build --source $DIR/project/dollar-docs/src/main/webapp/ --destination $DIR/dist/docs --config $DIR/project/dollar-docs/src/main/webapp/_config.yml,$DIR/project/dollar-docs/src/main/webapp/env.yml

cp -rf * $DIR/dist/docs || :
