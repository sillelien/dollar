#!/bin/bash -eu
cd $(dirname $0) && cd ..
PROJECT=$(pwd)
( redis-server /usr/local/etc/redis.conf || : ) &
REDIS_PID=$!
bin/build-dist.sh
bin/test-dist.sh
kill -2 ${REDIS_PID}
./pack/pack.sh
