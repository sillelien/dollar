#!/usr/bin/env bash
set-eux
cd $(dirname $0)
DIR=$(pwd)
./set-version.sh
mvn -q -T 1C -Drat.skip -Dsource.skip=true -DgenerateReports=false -Dmaven.javadoc.skip=true install
