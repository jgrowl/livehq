#!/usr/bin/env bash

cwd=$(pwd)
# Make sure we are in the script directory
# http://stackoverflow.com/questions/3349105/how-to-set-current-working-directory-to-the-directory-of-the-script
cd ${0%/*}
./setup-consul.sh
cd - && cd media


#./packer/scripts/setup-consul.sh
#cp /vagrant/media/lib/native/libjingle_peerconnection_so.so /usr/local/lib/libjingle_peerconnection.so

usermod -u $APP_UID app
groupmod -g $APP_GUID app
sudo -u app sbt -Djava.library.path=$LD_LIBRARY_PATH \
    -Dsbt.ivy.home=$cwd/.ivy \
    -Dakka.remote.netty.tcp.hostname=livehq-subscriber-monitor-seed \
    -Dakka.remote.netty.tcp.port=2554 \
    'run-main server.app.App subscriber-monitor -p 2554'
