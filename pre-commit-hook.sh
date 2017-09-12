#!/usr/bin/env bash
source ~/.bash_profile
mvn -Drat.skip=true -Dsource.skip=true -DgenerateReports=false clean install &> pre-commit.log
