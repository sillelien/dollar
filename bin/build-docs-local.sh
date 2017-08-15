#!/bin/bash -eux
cd $(dirname $0) && cd ..
PROJECT=$(pwd)
export RELEASE=dev

ls $PROJECT/dollar-docs/src/main/webapp/

envsubst < $PROJECT/dollar-docs/src/main/webapp/_env.yml > $PROJECT/dollar-docs/src/main/webapp/env.yml
echo "---">  $PROJECT/dollar-docs/src/main/webapp/_pages/manual.md
echo "layout: splash" >> $PROJECT/dollar-docs/src/main/webapp/_pages/manual.md
echo "title:  \"Dollar Scripting Language Manual\"" >> $PROJECT/dollar-docs/src/main/webapp/_pages/manual.md
echo "permalink: /manual/" >> $PROJECT/dollar-docs/src/main/webapp/_pages/manual.md
echo "---" >> $PROJECT/dollar-docs/src/main/webapp/_pages/manual.md

tail -n +2 $PROJECT/dollar-docs/src/main/resources/pages/manual.md  >> $PROJECT/dollar-docs/src/main/webapp/_pages/manual.md
mvn -q  -e -Dexec.mainClass="dollar.internal.runtime.script.parser.Symbols" install exec:java >> $PROJECT/dollar-docs/src/main/webapp/_pages/manual.md || :
cd $PROJECT/dollar-docs/src/main/webapp/
bundle exec jekyll serve --config _config.yml,env.yml
