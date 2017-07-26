#!/usr/bin/env bash
set -eux
cd $(dirname $0) && cd ..
PROJECT=$(pwd)

mkdir -p dist/devdocs
bin/set-version.sh
mvn -q -T 2C -Dmaven.test.skip -DskipDeploy -DstagingDirectory=dist/devdocs/ install site:site site:stage
