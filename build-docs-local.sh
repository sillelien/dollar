#!/bin/bash -eux
cd $(dirname $0)
DIR=$(pwd)
export RELEASE=dev

ls $DIR/dollar-docs/src/main/webapp/

envsubst < $DIR/dollar-docs/src/main/webapp/_env.yml > $DIR/dollar-docs/src/main/webapp/env.yml

jekyll serve --source $DIR/dollar-docs/src/main/webapp/ --config $DIR/dollar-docs/src/main/webapp/_config.yml,$DIR/dollar-docs/src/main/webapp/env.yml
