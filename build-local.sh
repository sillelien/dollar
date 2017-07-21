#!/bin/bash -eu
GITHUB_PASSWORD=$(cat ~/.github | tail -1 | cut -d= -f2)
circleci build -e GITHUB_PASSWORD=$GITHUB_PASSWORD \
               -e GIT_EMAIL=hello@neilellis.me \
               -e GIT_NAME=neilellis \
               -e DOCKER_EMAIL=hello@neilellis.me \
               -e DOCKER_USER=neilellis \
               -e DOCKER_PASS=$DOCKER_PASS \
               -e CI_LOCAL=true

