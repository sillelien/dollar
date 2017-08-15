#!/usr/bin/env bash
set -eux
cd $(dirname $0) && cd ..
PROJECT=$(pwd)

mkdir -p /home/circleci/workspace/devdocs
mkdir -p /home/circleci/workspace/gendocs
bin/set-version.sh
mvn -q -T 2C -Dmaven.test.skip -DskipDeploy -DstagingDirectory=/home/circleci/workspace/devdocs/ install site:site site:stage
mvn -q exec:java -e -Dexec.mainClass="dollar.internal.runtime.script.parser.Symbols" > /home/circleci/workspace/gendocs/symbols.md

