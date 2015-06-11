livehq
=====================================

#Development Instructions

## Install vagrant plugins

```
vagrant plugin install vagrant-triggers
```

## Vagrant up

``` 
vagrant up
```

## Building media image

By default vagrant will download a pre-built media image from the docker registry.  
To build the image yourself, perform the following steps:

```
(cd media/packer && exec packer build livehq-media-packer.json)
```

### Packer bug

Note that docker versions later than 1.3.3 currently break packer's ability to upload files.  
You will either need to use Docker v1.3.3 or use a patched version of packer.  
There is a fix implemented packer's master branch so the fix is on its way likely in the next release.

See https://github.com/mitchellh/packer/issues/1752#issuecomment-108792425

# Deploying

## Dependencies

- docker-compose

## Set Environment Variables
 
    export DOCKER_CERT_PATH=~/.joyent  
    export DOCKER_HOST=tcp://aaa.bbb.ccc.ddd:4243

## Push

```
vagrant push
```

