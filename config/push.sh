#!/bin/bash -eux

export COMMON_USER=ubuntu
export COMMON_HOST=165.225.138.46

export DOCKER_CERT_PATH=~/.joyent
export DOCKER_HOST=tcp://$COMMON_HOST:4243

export DOCKER_TLS_VERIFY=0
export DOCKER_TLS=1

docker-compose stop consul registrator
docker-compose rm -f consul registrator
docker-compose up -d consul registrator

#docker-compose stop turnserver
#docker-compose rm -f turnserver
#docker-compose up -d turnserver

docker-compose stop redis
docker-compose rm -f redis
docker-compose up -d redis

docker-compose stop mongodb
docker-compose rm -f mongodb
docker-compose up -d mongodb

docker-compose stop publisher publishermonitor subscriber subscribermonitor
docker-compose rm -f publisher publishermonitor subscriber subscribermonitor

# Build media executable jar
(cd media && sbt clean assembly)
docker-compose build media

docker-compose up -d publisher publishermonitor subscriber subscribermonitor

docker-compose stop signal
docker-compose rm -f signal
docker-compose build signal
docker-compose up -d signal

docker-compose stop api
docker-compose rm -f api
docker-compose build api
docker-compose up -d api

# Build web
(cd web && pub build --mode=debug)

docker-compose stop web
docker-compose rm -f web
docker-compose build web
docker-compose up -d web
