#!/usr/bin/env bash
CONSUL_IP=$(getent hosts livehq-consul-server | awk '{ print $1 }')
NAMESERVER="nameserver ${CONSUL_IP}"

if grep -Fxq "$NAMESERVER" /etc/resolv.conf
then
    true
else
    echo -e "${NAMESERVER}\n$(cat /etc/resolv.conf)" > /etc/resolv.conf
fi
