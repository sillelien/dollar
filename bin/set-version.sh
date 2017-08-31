#!/usr/bin/env bash
set -eux

cd $(dirname $0) && cd ..
PROJECT=$(pwd)

[[ ${CIRCLE_BRANCH:-dev} != 'master' ]] || ( mvn -q versions:set -DnewVersion=$(cat .release) && mvn -q versions:resolve-ranges && mvn -q versions:lock-snapshots )
