#!/bin/bash -eux
cd $(dirname $0) && cd ..
PROJECT=$(pwd)
export RELEASE=dev

ls $PROJECT/dollar-docs/src/main/webapp/

envsubst < $PROJECT/dollar-docs/src/main/webapp/_env.yml > $PROJECT/dollar-docs/src/main/webapp/env.yml
tail -n +2 $PROJECT/dollar-docs/src/main/resources/pages/scripting.md  > $PROJECT/dollar-docs/src/main/webapp/scripting.md
jekyll serve --source $PROJECT/dollar-docs/src/main/webapp/ --config $PROJECT/dollar-docs/src/main/webapp/_config.yml,$PROJECT/dollar-docs/src/main/webapp/env.yml
