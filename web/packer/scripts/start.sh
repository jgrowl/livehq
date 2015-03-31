#!/bin/sh

#ls -l /etc/profile.d
#echo $PATH

usermod -u $APP_UID app
groupmod -g $APP_GUID app

sudo -u app pub get
sudo -u app pub serve

