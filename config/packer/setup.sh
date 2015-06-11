#!/bin/sh

packer build base/livehq-base-packer.json
packer build media/livehq-base-packer.json
