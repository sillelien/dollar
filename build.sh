#!/usr/bin/env bash
if [[ -f ~/.bash_profile ]]
then
    source ~/.bash_profile
fi
export MAVEN_OPTS="$MAVEN_OPTS -XX:+TieredCompilation -XX:TieredStopAtLevel=1"
mvn -Drat.skip=true -Dmaven.source.skip=true -Dsource.skip=true -DgenerateReports=false -Dmaven.javadoc.skip=true -pl dollar-examples -am --offline install
