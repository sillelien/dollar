#!/bin/bash -eux
cd $(dirname $0)
DIR=$(pwd)
echo "login=neilellis" > ~/.github
echo "password=${GITHUB_PASSWORD}" >> ~/.github

mvn versions:set -DnewVersion=$(cat .release)
[ ! -d dist ] || rm -rf dist
mkdir dist
cd dist
git clone https://github.com/sillelien/dollar docs
cd docs
git checkout gh-pages
jekyll build --source $DIR/dollar-docs/src/main/webapp/ --destination .
cp -rf * $DIR/dist/docs || :
[ -d $DIR/dist/docs/dev ] || mkdir -p $DIR/dist/docs/dev
cd $DIR
mvn -q  install
mvn -q -Dmaven.test.skip -DskipDeploy -DstagingDirectory=$DIR/dist/docs/dev/ site:site site:stage
sleep 60
mvn  exec:java -e -pl com.sillelien:dollar-docs -Dexec.mainClass="com.sillelien.dollar.docs.ParseDocs" -Dexec.args="./dist/docs"
cd dist/docs
git add *
git commit -a -m "Updated docs from build"  || :
git push || :
cd -

