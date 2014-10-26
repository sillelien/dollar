#!/bin/bash -eux
cd $(dirname $0)
[ -d ~/.dollar-cache ] || mkdir ~/.dollar-cache
[ -f ~/.dollar-cache/zulu-mac.zip ] || curl -H "Referer: http://www.azulsystems.com/products/zulu/downloads#mac"  http://cdn.azulsystems.com/zulu/2014-09-8.3-bin/zulu1.8.0_20-8.3.0.4-macosx.zip   > ~/.dollar-cache/zulu-mac.zip
[ -d ../dist ] && rm -rf ../dist || :
jar=$(ls ../dollar-runtime/target/dollar-runtime*-fat.jar)
dist_osx=../dist/osx/dollar
mkdir -p ${dist_osx}
cp -r ../dist-skel/common/* ${dist_osx}
cp -r ../dist-skel/osx/* ${dist_osx}
cp ../LICENSE ../README.md $dist_osx
mkdir $dist_osx/lib
cp $jar $dist_osx/lib
cd $dist_osx
unzip ~/.dollar-cache/zulu-mac.zip -d .
mv zulu* jdk
cd ..
tar -zcvf dollar.tgz dollar





