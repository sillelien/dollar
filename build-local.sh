#!/bin/bash -eu
cd $(dirname $0)
redis-server /usr/local/etc/redis.conf &
REDIS_PID=$!
mvn clean install
./build-docs.sh
kill -2 ${REDIS_PID}
./pack/pack.sh
