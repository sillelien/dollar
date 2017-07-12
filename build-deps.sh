#!/bin/bash -eux
cd $(dirname $0)
DIR=$(pwd)
cd -
mvn -v
mkdir -p ~/.m2/
echo "login=neilellis" > ~/.github
echo "password=${GITHUB_PASSWORD}" >> ~/.github

cp $DIR/settings.xml ~/.m2/settings.xml
$DIR/set-version.sh
mvn install -e -q -Drat.skip -Dsource.skip=true -DgenerateReports=false -Dmaven.javadoc.skip=true -Dmaven.test.skip
