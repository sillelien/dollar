#!/usr/bin/env bash
mvn -Drat.skip=true -Dsource.skip=true -DgenerateReports=false clean install &> pre-commit.log
