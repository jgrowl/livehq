#!/bin/sh

export PATH=/usr/lib/dart/bin/:$PATH;
##export PUB_CACHE=/home/app/.pub-cache
export PUB_CACHE=/vagrant/.pub-cache
##dart --version
#pub --trace 'get'
pub get
pub serve --hostname=0.0.0.0 --mode=debug --trace
