#!/bin/bash -eu
cd $(dirname $0)
[ ! -d dist ] || rm -rf dist
mkdir dist
cd dist
git clone git@github.com:neilellis/dollar.git docs
cd docs
git checkout gh-pages
cp -rf ../../dollar-docs/src/main/webapp/* .
cd ../..
mvn exec:java -q -pl me.neilellis:dollar-docs -Dexec.mainClass="me.neilellis.dollar.docs.ParseDocs" -Dexec.args="./dist/docs"
cd dist/docs
git add *
git commit -a -m "Updated docs from build"
git push
cd -

