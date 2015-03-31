#!/usr/bin/env bash
usermod -u $APP_UID app
groupmod -g $APP_GUID app
sudo -u app ./packer/scripts/app.sh
