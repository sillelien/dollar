#!/bin/bash -eux
cd $(dirname $0) && cd ..
PROJECT=$(pwd)
export RELEASE=dev

ls $PROJECT/docs/

envsubst < $PROJECT/docs/_env.yml > $PROJECT/docs/env.yml
echo "---">  $PROJECT/docs/_pages/manual.md
echo "layout: single" >> $PROJECT/docs/_pages/manual.md
echo "title: \"Dollar Scripting Language Manual\"" >> $PROJECT/docs/_pages/manual.md
echo "permalink: /manual/" >> $PROJECT/docs/_pages/manual.md
echo "---" >> $PROJECT/docs/_pages/manual.md

tail -n +2 $PROJECT/dollar-docs/src/main/resources/pages/manual.md  >> $PROJECT/docs/_pages/manual.md
mvn -q  -e -Dexec.mainClass="dollar.internal.runtime.script.parser.Symbols" install exec:java >> $PROJECT/docs/_pages/manual.md || :
cd $PROJECT/docs/
bundle exec jekyll serve --config _config.yml,env.yml
