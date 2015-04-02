#!/bin/sh

./scripts/setup-consul.sh
bundle

# Sleeping here is ugly but I need to make sure the redis is up before starting.
#TODO: Look for better way to determine whether redis is up
sleep 5

#apt-get -y update
#apt-get install -y redis-tools
#redis-cli -h livehq-redis -p 6379 ping

ruby server.rb
