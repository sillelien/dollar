#!/bin/bash
mvn -T 2C -Drat.skip -Dsource.skip=true -DgenerateReports=false -Dmaven.javadoc.skip=true -Dmaven.test.skip -Denforcer.skip clean package
