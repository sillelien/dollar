#!/bin/bash -eux
cd $(dirname $0)
DIR=$(pwd)
[ ! -d dist ] || rm -rf dist
mkdir dist
cd dist
git clone https://github.com/sillelien/dollar docs
cd docs
git checkout gh-pages
cd $DIR/dollar-docs/src/main/webapp/
jekyll build
[ -d $DIR/dist/docs/dev ] || mkdir -p $DIR/dist/docs/dev
cp -rf $DIR/target/staging/* $DIR/dist/docs/dev
cp -rf * $DIR/dist/docs
cd $DIR
mvn -q install site exec:java -e -pl com.sillelien:dollar-docs -Dexec.mainClass="com.sillelien.dollar.docs.ParseDocs" -Dexec.args="./dist/docs"
cd dist/docs
git add *
git commit -a -m "Updated docs from build"  || :
git push || :
cd -

