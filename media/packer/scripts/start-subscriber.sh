#!/usr/bin/env bash
cp /vagrant/media/lib/native/libjingle_peerconnection_so.so /usr/local/lib/libjingle_peerconnection.so
usermod -u $APP_UID app
groupmod -g $APP_GUID app
sudo -u app sbt -Djava.library.path=$LD_LIBRARY_PATH \
    -Dakka.remote.netty.tcp.hostname=livehq-subscriber-seed \
    -Dakka.remote.netty.tcp.port=2553 \
    'run-main server.app.App subscriber -p 2553'
