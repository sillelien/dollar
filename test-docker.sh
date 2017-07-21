#!/bin/bash -eux
cd $(dirname $0)
DIR=$(pwd)
alias dollar="docker run -it sillelien/dollarscript-headless:${MAJOR_VERSION} -v /build:$(pwd)"
[ -d /tmp/docker_test ] || mkdir /tmp/docker_test
cp $DIR/dollar-examples/src/main/resources/test_*.ds  /tmp/docker_test
cd /tmp/docker_test
for file in $(ls)
do
    echo "Testing: " $file
    dollar ${file}
done

