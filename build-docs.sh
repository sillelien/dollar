#!/bin/bash -eux
cd $(dirname $0)
DIR=$(pwd)
echo "login=neilellis" > ~/.github
echo "password=${GITHUB_PASSWORD}" >> ~/.github
./set-version.sh
mkdir -p dist
cd dist
git clone https://github.com/sillelien/dollar docs
cd docs
git checkout gh-pages
jekyll build --source $DIR/dollar-docs/src/main/webapp/ --destination .
cp -rf * $DIR/dist/docs || :
[ -d $DIR/dist/docs/dev ] || mkdir -p $DIR/dist/docs/dev
cd $DIR
mvn -q -Dmaven.test.skip -DskipDeploy -DstagingDirectory=$DIR/dist/docs/dev/ install site:site site:stage
mvn  exec:java -e -pl com.sillelien:dollar-docs -Dexec.mainClass="com.sillelien.dollar.docs.ParseDocs" -Dexec.args="./dist/docs"
cd dist/docs
git add *
git commit -a -m "Updated docs from build"  || :
git push || :
cd -

