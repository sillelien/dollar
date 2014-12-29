#!/bin/bash -eu
cd $(dirname $0)
DIR=$(pwd)
[ ! -d dist ] || rm -rf dist
mkdir dist
cd dist
git clone git@github.com:neilellis/dollar.git docs
cd docs
git checkout gh-pages
cd $DIR/dollar-docs/src/main/webapp/
jekyll build
cp -rf $DIR/target/staging $DIR/dist/docs/reports
cp -rf * $DIR/dist/docs
cd $DIR
mvn -q install exec:java -e -pl me.neilellis:dollar-docs -Dexec.mainClass="me.neilellis.dollar.docs.ParseDocs" -Dexec.args="./dist/docs"
cd dist/docs
git add *
git commit -a -m "Updated docs from build"  || :
git push || :
cd -

