#!/usr/bin/env bash
./packer/scripts/setup-consul.sh
cp /vagrant/media/lib/native/libjingle_peerconnection_so.so /usr/local/lib/libjingle_peerconnection.so
usermod -u $APP_UID app
groupmod -g $APP_GUID app
sudo -u app sbt -Djava.library.path=$LD_LIBRARY_PATH \
    -Dakka.remote.netty.tcp.hostname=livehq-publisher-seed \
    -Dakka.remote.netty.tcp.port=2551 \
    'run-main server.App publisher -p 2551'


#apt-get install -y dnsutils
#nslookup livehq-publisher-seed
#ifconfig
#apt-get install -y redis-tools
#redis-cli -h livehq-redis -p 6379 ping
