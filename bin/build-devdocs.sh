#!/usr/bin/env bash
set -eux
cd $(dirname $0) && cd ..
PROJECT=$(pwd)

mkdir -p /home/circleci/workspace/devdocs
mkdir -p /home/circleci/workspace/gendocs
bin/set-version.sh
mvn -q -T 2C -Dmaven.test.skip -DskipDeploy -DstagingDirectory=/home/circleci/workspace/devdocs/ install site:site site:stage
cd dollar-script
mvn package
mvn -q  -e -Dexec.mainClass="dollar.internal.runtime.script.parser.Symbols" install exec:java > /home/circleci/workspace/gendocs/symbols.md
cd -
