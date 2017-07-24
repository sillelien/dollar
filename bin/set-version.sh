#!/usr/bin/env bash
set -eux

cd $(dirname $0) && cd ..
PROJECT=$(pwd)

[[ ${CIRCLE_BRANCH:-dev} != 'master' ]] || ( mvn versions:set -DnewVersion=$(cat .release) && mvn versions:resolve-ranges && mvn versions:lock-snapshots )
