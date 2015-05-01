#!/bin/sh
./packer/scripts/setup-consul.sh

usermod -u $APP_UID app
groupmod -g $APP_GUID app

sudo -u app ./packer/scripts/pub.sh
