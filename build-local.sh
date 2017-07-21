#!/bin/bash -eu
GITHUB_PASSWORD=$(cat ~/.github | tail -1 | cut -d= -f2)
circleci build -e GITHUB_PASSWORD=$GITHUB_PASSWORD
