#!/usr/bin/env bash
./packer/scripts/setup-consul.sh
cp /vagrant/media/lib/native/libjingle_peerconnection_so.so /usr/local/lib/libjingle_peerconnection.so
usermod -u $APP_UID app
groupmod -g $APP_GUID app
sudo -u app sbt -Djava.library.path=$LD_LIBRARY_PATH \
    -Dakka.remote.netty.tcp.hostname=livehq-subscriber-monitor-seed \
    -Dakka.remote.netty.tcp.port=2554 \
    'run-main server.app.App subscriber-monitor -p 2554'
