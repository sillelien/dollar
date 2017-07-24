#!/bin/bash -eux
cd $(dirname $0) && cd ..
PROJECT=$(pwd)
cd -
mvn -v
mkdir -p ~/.m2/

./bin/create-github-file.sh
cp settings.xml ~/.m2/settings.xml
./bin//set-version.sh
mvn install -e -q -Drat.skip -Dsource.skip=true -DgenerateReports=false -Dmaven.javadoc.skip=true -Dmaven.test.skip
