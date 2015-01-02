#!/bin/bash -eu
cd $(dirname $0)
#mvn install -Dmaven.test.skip=true
./pack/pack.sh
[ -d target/build_test ] || mkdir target/build_test
cp ./dollar-examples/src/main/resources/test_*.ds  target/build_test
for file in $(ls target/build_test)
do
    echo "Testing: " $file
    ./dist/osx/dollar/bin/dollar target/build_test/${file} || :
done
