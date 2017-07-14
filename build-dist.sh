#!/bin/bash -eux
cd $(dirname $0)
PROJECT=$(pwd)

./set-version.sh

mvn -q -Dmaven.test.skip -Drat.skip -Dsource.skip=true -DgenerateReports=false -Dmaven.javadoc.skip=true  install

DIST=dist/dollar
if [[ -n $(ls $DIST) ]]; then
    rm -r ${DIST}/*
fi

[ -d ${DIST}/plugins ] || mkdir -p ${DIST}/plugins
RUNTIME_JAR=$(ls ${PROJECT}/dollar-runtime/target/dollar-runtime*-mod.jar)

mkdir -p ${DIST}

for PLUGIN in $(ls ${PROJECT}/dollar-plugins)
do
    if [ -d ${PROJECT}/dollar-plugins/${PLUGIN}/target ]
    then
        cp -f ${PROJECT}/dollar-plugins/${PLUGIN}/target/*plugin.jar ${DIST}/plugins
    fi
done

cp -r ${PROJECT}/dist-skel/common/* ${DIST}
cp ${PROJECT}/LICENSE $PROJECT/README.md ${DIST}
mkdir ${DIST}/lib
cp ${RUNTIME_JAR} ${DIST}/lib/dollar-runtime.jar

SLF4J_VERSION=1.7.12
LOG4J_VERSION=1.2.17
XSTREAM_VERSION=1.4.8

cp -f ~/.m2/repository/org/slf4j/slf4j-api/${SLF4J_VERSION}/slf4j-api-${SLF4J_VERSION}.jar ${DIST}/lib
cp -f ~/.m2/repository/org/slf4j/slf4j-log4j12/${SLF4J_VERSION}/slf4j-log4j12-${SLF4J_VERSION}.jar ${DIST}/lib
cp -f ~/.m2/repository/log4j/log4j/${LOG4J_VERSION}/log4j-${LOG4J_VERSION}.jar ${DIST}/lib
cp -f ~/.m2/repository/com/thoughtworks/xstream/xstream/${XSTREAM_VERSION}/xstream-${XSTREAM_VERSION}.jar  ${DIST}/lib
cp ${PROJECT}/.release ${DIST}/RELEASE
cd ${DIST}
cd ..
tar -zcvf dollar.tgz dollar





