#!/bin/sh

#cwd=$(pwd)
# Make sure we are in the script directory
# http://stackoverflow.com/questions/3349105/how-to-set-current-working-directory-to-the-directory-of-the-script
cd ${0%/*}
./setup-consul.sh
cd - && cd signal

#./scripts/setup-consul.sh
bundle

# Sleeping here is ugly but I need to make sure the redis is up before starting.
#TODO: Look for better way to determine whether redis is up
sleep 5

#apt-get -y update
#apt-get install -y redis-tools
#redis-cli -h livehq-redis -p 6379 ping

ruby server.rb
