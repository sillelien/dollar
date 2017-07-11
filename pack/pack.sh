#!/bin/bash -eux
cd $(dirname $0)
[ -d ../dist ] && rm -rf ../dist || :
dist=../dist/dollar
[ -d ${dist}/plugins ] || mkdir -p ${dist}/plugins
jar=$(ls ../dollar-runtime/target/dollar-runtime*-mod.jar)

mkdir -p ${dist}

for pd in $(ls ../dollar-plugins)
do
    if [ -d ../dollar-plugins/${pd}/target ]
    then
        cp -f ../dollar-plugins/${pd}/target/*plugin.jar ${dist}/plugins
    fi
done

cp -r ../dist-skel/common/* ${dist}
cp ../LICENSE ../README.md ${dist}
mkdir ${dist}/lib
cp ${jar} ${dist}/lib

SLF4J_VERSION=1.7.12
LOG4J_VERSION=1.2.17
XSTREAM_VERSION=1.4.8

cp -f ~/.m2/repository/org/slf4j/slf4j-api/${SLF4J_VERSION}/slf4j-api-${SLF4J_VERSION}.jar ${dist}/lib
cp -f ~/.m2/repository/org/slf4j/slf4j-log4j12/${SLF4J_VERSION}/slf4j-log4j12-${SLF4J_VERSION}.jar ${dist}/lib
cp -f ~/.m2/repository/log4j/log4j/${LOG4J_VERSION}/log4j-${LOG4J_VERSION}.jar ${dist}/lib
cp -f ~/.m2/repository/com/thoughtworks/xstream/xstream/${XSTREAM_VERSION}/xstream-${XSTREAM_VERSION}.jar  ${dist}/lib

cd ${dist}
cd ..
tar -zcvf dollar.tgz dollar





