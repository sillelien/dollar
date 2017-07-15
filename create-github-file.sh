#!/usr/bin/env bash
if [[ -n ${CI:-} ]] ; then
    echo "login=neilellis" > ~/.github
    echo "password=${GITHUB_PASSWORD}" >> ~/.github
fi
