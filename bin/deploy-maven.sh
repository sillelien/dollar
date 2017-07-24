#!/usr/bin/env bash
set -eux
cd $(dirname $0) && cd ..
PROJECT=$(pwd)

git config --global user.email "hello@neilellis.me"
git config --global user.name "Neil Ellis"
bin/set-version.sh
mvn -q -T 1C -Dmaven.test.skip=true -Drat.skip=true -DskipEnforcer -Dmaven.javadoc.skip=true -DgenerateReports=false package
mvn -q -T 2C -Dorg.xml.sax.driver=com.sun.org.apache.xerces.internal.parsers.SAXParser -Dmaven.test.skip=true -Drat.skip=true -DskipEnforcer -Dmaven.javadoc.skip=true -DgenerateReports=false deploy
