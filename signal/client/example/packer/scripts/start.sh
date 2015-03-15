#!/bin/sh

#ls -l /etc/profile.d
#echo $PATH

pub get
pub serve --hostname=0.0.0.0 --mode=debug

