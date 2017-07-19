#!/bin/bash -eu
cd $(dirname $0)
( redis-server /usr/local/etc/redis.conf || : ) &
REDIS_PID=$!
./build-dist.sh
./test-dist.sh
kill -2 ${REDIS_PID}
./pack/pack.sh
