#!/usr/bin/env bash
mvn -q -T 1C -Drat.skip -Dsource.skip=true -DgenerateReports=false clean install &> pre-commit.log
