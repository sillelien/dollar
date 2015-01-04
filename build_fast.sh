#!/bin/bash
mvn -T 2C -Drat.skip -Dsource.skip=true -DgenerateReports=false -Dmaven.javadoc.skip=true -Denforcer.skip clean package
