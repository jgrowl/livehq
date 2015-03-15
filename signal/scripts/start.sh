#!/bin/sh

# Sleeping here is ugly but I need to make sure the redis is up before starting.
#TODO: Look for better way to determine whether redis is up
sleep 5

ruby server.rb
