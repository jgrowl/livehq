#!/bin/bash -eux

apt-get install -y python-software-properties software-properties-common
apt-get install -y ansible
ansible-galaxy install php-coder.oraclejdK
ansible-galaxy install JasonGiedymin.sbt