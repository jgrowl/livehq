#!/bin/bash -eux

export COMMON_USER=ubuntu
export COMMON_HOST=165.225.136.204

export DOCKER_CERT_PATH=~/.joyent
export DOCKER_HOST=tcp://$COMMON_HOST:4243

export DOCKER_TLS_VERIFY=0
export DOCKER_TLS=1

docker-compose stop consul registrator
docker-compose rm -f consul registrator
docker-compose up -d consul registrator

docker-compose stop turnserver
docker-compose rm -f turnserver
docker-compose up -d turnserver

docker-compose stop redis
docker-compose rm -f redis
docker-compose up -d redis

docker-compose stop mongodb
docker-compose rm -f mongodb
docker-compose up -d mongodb

# Build media executable jar
cd media
sbt assembly
cd -

docker-compose stop publisher
docker-compose rm -f publisher
docker-compose build publisher
docker-compose up -d publisher

docker-compose stop publishermonitor
docker-compose rm -f publishermonitor
docker-compose build publishermonitor
docker-compose up -d publishermonitor

docker-compose stop subscriber
docker-compose rm -f subscriber
docker-compose build subscriber
docker-compose up -d subscriber

docker-compose stop subscribermonitor
docker-compose rm -f subscribermonitor
docker-compose build subscribermonitor
docker-compose up -d subscribermonitor

docker-compose stop signal
docker-compose rm -f signal
docker-compose build signal
docker-compose up -d signal

docker-compose stop api
docker-compose rm -f api
docker-compose build api
docker-compose up -d api

#cd web
#pub build --mode=debug
#cd -

(cd web && pub build --mode=debug)

docker-compose stop web
docker-compose rm -f web
docker-compose build web
docker-compose up -d web
