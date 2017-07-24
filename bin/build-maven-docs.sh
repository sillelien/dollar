#!/bin/bash -eux
cd $(dirname $0) && cd ..
PROJECT=$(pwd)
bin/create-github-file.sh
bin/set-version.sh
mkdir -p dist
cd dist
git clone https://github.com/sillelien/dollar docs
cd docs
git checkout gh-pages
cp -rf * $PROJECT/dist/docs || :
[ -d $PROJECT/dist/docs/dev ] || mkdir -p $PROJECT/dist/docs/dev
cd $PROJECT
mvn -q -Dmaven.test.skip -DskipDeploy -DstagingDirectory=$PROJECT/dist/docs/dev/ install site:site site:stage
mvn  exec:java -e -pl com.sillelien:dollar-docs -Dexec.mainClass="com.sillelien.dollar.docs.ParseDocs" -Dexec.args="./dist/docs"
cd dist/docs
git add *
git commit -a -m "Updated docs from build"  || :
git push || :
cd -

