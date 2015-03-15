#!/bin/bash -eux

sudo add-apt-repository ppa:rquillo/ansible
sudo apt-get update
apt-get install -y ansible
