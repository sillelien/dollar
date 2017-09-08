#!/bin/bash -eux
cd $(dirname $0) && cd ..
PROJECT=$(pwd)
[ -d target/build_test ] || mkdir target/build_test
cp $PROJECT/dollar-examples/src/main/resources/quick/*.ds  target/build_test
for file in $(ls target/build_test)
do
    echo "Testing: " $file
    $HOME/workspace/dollar/bin/dollar target/build_test/${file}
done

