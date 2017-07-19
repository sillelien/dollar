#!/bin/bash -eux
cd $(dirname $0)
PROJECT=$(pwd)

mvn -q -Drat.skip -Dsource.skip=true -DgenerateReports=false -Dmaven.javadoc.skip=true  clean install

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
GUAVA_VERSION=22.0
MAPDB_VERSION=0.1.6
CAMEL_VERSION=0.1.6
cp -f ~/.m2/repository/org/slf4j/slf4j-api/${SLF4J_VERSION}/slf4j-api-${SLF4J_VERSION}.jar ${DIST}/lib
cp -f ~/.m2/repository/org/slf4j/slf4j-log4j12/${SLF4J_VERSION}/slf4j-log4j12-${SLF4J_VERSION}.jar ${DIST}/lib
cp -f ~/.m2/repository/log4j/log4j/${LOG4J_VERSION}/log4j-${LOG4J_VERSION}.jar ${DIST}/lib
cp -f ~/.m2/repository/com/google/guava/guava/${GUAVA_VERSION}/guava-${GUAVA_VERSION}.jar ${DIST}/lib
cp -f ~/.m2/repository/com/sillelien/dollar-mapdb/${MAPDB_VERSION}/dollar-mapdb-${MAPDB_VERSION}.jar ${DIST}/lib/dollar-mapdb.jar
cp -f ~/.m2/repository/com/sillelien/dollar-camel/${CAMEL_VERSION}/dollar-camel-${CAMEL_VERSION}.jar ${DIST}/lib/dollar-camel.jar

cp ${PROJECT}/.release ${DIST}/RELEASE
cd ${DIST}
cd ..
tar -zcvf dollar.tgz dollar





