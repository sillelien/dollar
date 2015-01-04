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
cp -f ~/.m2/repository/org/slf4j/slf4j-api/1.7.7/slf4j-api-1.7.7.jar ${dist}/lib
cp -f ~/.m2/repository/org/slf4j/slf4j-log4j12/1.7.7/slf4j-log4j12-1.7.7.jar ${dist}/lib
cp -f ~/.m2/repository/log4j/log4j/1.2.17/log4j-1.2.17.jar ${dist}/lib
cp -f ~/.m2/repository/com/thoughtworks/xstream/xstream/1.4.7/xstream-1.4.7.jar  ${dist}/lib
cd ${dist}
cd ..
tar -zcvf dollar.tgz dollar





