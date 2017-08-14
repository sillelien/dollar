#!/usr/bin/env bash
set -eux
cd $(dirname $0) && cd ..
PROJECT=$(pwd)

mkdir -p /home/circleci/dist/devdocs
mkdir -p /home/circleci/dist/gendocs
bin/set-version.sh
mvn -q -T 2C -Dmaven.test.skip -DskipDeploy -DstagingDirectory=dist/devdocs/ install site:site site:stage
mvn  exec:java -e -pl com.sillelien:dollar-docs -Dexec.mainClass="dollar.internal.runtime.script.parser.Symbols" > dist/gendocs/symbols.md

