#!/bin/bash -eux


cd $(dirname $0)
[ -d ~/.dollar-cache ] || mkdir ~/.dollar-cache
[ -f ~/.dollar-cache/zulu-mac.zip ] || curl -H "Referer: http://www.azulsystems.com/products/zulu/downloads#mac"  http://cdn.azulsystems.com/zulu/2014-09-8.3-bin/zulu1.8.0_20-8.3.0.4-macosx.zip   > ~/.dollar-cache/zulu-mac.zip

jar=$(ls ../dollar-runtime/target/dollar-runtime*-fat.jar)
java -jar packr.jar \
     -platform mac \
     -jdk ~/.dollar-cache/zulu-mac.zip \
     -executable dollar \
     -appjar ${jar} \
     -mainclass "me/neilellis/dollar/runtime/Main" \
     -vmargs "-Xmx1G" \
     -resources "../LICENSE;../README.md;../dollar-examples" \
     -outdir ../target/dist

[ -d ../dist ] && rm -rf ../dist || :
mkdir -p ../dist/osx/dollar
cp -r ../target/dist/Contents/MacOS/* ../dist/osx/dollar
cd ../dist/osx
tar -zcvf dollar.tgz dollar
cd -





