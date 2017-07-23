#!/bin/bash -eux
cd $(dirname $0)
DIR=$(pwd)
alias dollar=""
[ -d .tmp.docker_test ] || mkdir .tmp.docker_test
cp $DIR/dollar-examples/src/main/resources/test_*.ds  .tmp.docker_test
cd .tmp.docker_test
for file in $(ls | grep -v redis)
do
    echo "Testing: " $file
    docker run -v $HOME/.github:/root/.github -v $HOME/.dollar:/root/.dollar -v $(pwd):/build -it sillelien/dollarscript-headless:${MAJOR_VERSION}  /build/${file}
done

