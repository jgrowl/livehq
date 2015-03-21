#!/usr/bin/env bash
cp /vagrant/media/libs/native/libjingle_peerconnection_so.so /usr/local/lib
sbt 'run-main server.App'
