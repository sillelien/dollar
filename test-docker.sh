#!/bin/bash -eux
cd $(dirname $0)
DIR=$(pwd)
alias dollar="docker run -it sillelien/dollarscript-headless:${MAJOR_VERSION} -v /build:$(pwd)"
[ -d target/build_test ] || mkdir target/build_test
cp $DIR/dollar-examples/src/main/resources/test_*.ds  target/build_test
for file in $(ls target/build_test)
do
    echo "Testing: " $file
    dollar target/build_test/${file}
done

