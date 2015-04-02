#!/usr/bin/env bash
./packer/scripts/setup-consul.sh

usermod -u $APP_UID app
groupmod -g $APP_GUID app

sudo -u app ./packer/scripts/app.sh
