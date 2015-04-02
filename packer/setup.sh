#!/bin/sh

#packer build livehq.json
#packer build livehq.json

packer build base/livehq-base-packer.json
packer build ruby/livehq-ruby-packer.json

packer build web/livehq-web-packer.json