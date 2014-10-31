#!/bin/bash -eu
cd $(dirname $0)
mvn clean install
./build-docs.sh
./pack/pack.sh
