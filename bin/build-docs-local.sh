#!/bin/bash -eu
cd $(dirname $0) && cd ..
PROJECT=$(pwd)

export RELEASE=dev

function manual() {
echo "---"
echo "layout: single"
echo "title: \"Dollar Scripting Language Manual\""
echo "permalink: /manual/"
echo "---"
echo
echo "{% include toc %}"
echo
tail -n +2 $PROJECT/dollar-docs/src/main/resources/pages/manual.md
cd dollar-script &> /dev/null
mvn -q  -e -Dexec.mainClass="dollar.internal.runtime.script.parser.Symbols" compile exec:java
cd - &> /dev/null
}

manual
manual > /tmp/manual.md
cp -f /tmp/manual.md $PROJECT/docs/_pages/manual.md
echo "cd $PROJECT/docs/ && bundle exec jekyll serve"
