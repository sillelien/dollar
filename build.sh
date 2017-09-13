#!/usr/bin/env bash
if [[ -f ~/.bash_profile ]]
then
    source ~/.bash_profile
fi
export MAVEN_OPTS="$MAVEN_OPTS -XX:+TieredCompilation -XX:TieredStopAtLevel=1"
mvn -DfailIfNoTests=false  -Dtest=DocTest,ParserQuickTest -pl dollar-examples -am --offline clean test
