ansible-tower-packer
====================

Build ansible tower with packer and push image to a docker registry. Currently only docker post-processors is used.

# Requirements

Packer 0.6.1+

# Build instructions

## Download Ansible Tower and extract to playbooks/ansible-tower

`wget http://releases.ansible.com/ansible-tower/setup/ansible-tower-setup-latest.tar.gz`

`mkdir playbooks/ansible-tower`

`tar -xvf ansible-tower-setup-latest.tar.gz -C playbooks/ansible-tower --strip 1`

## Change default passwords in local.yml

`pg_password: AWsecret`

`admin_password: password`

`rabbitmq_password: "AWXbunnies"`

## Change repository url in variables.json

`"repository_url": "localhost:5000/your_namespace/tower"`

## Build image 

sudo packer build -var-file=variables.json packer.json

## Run container with my_init command

/sbin/my_init is required to start up Apache, PostgreSQL, RabbitMQ, etc. For example:

`docker run  -p 44301:443 -p 8081:80 -t -i 1044803d6fd5 /sbin/my_init -- bash -l`

# TODO

Create individual containers for Apache, PostgreSQL, RabbitMQ, etc.

Create script to download and extract ansible tower to correct directory

# Disclaimer (IANAL)

Ansible Tower is a proprietary product and all rights regarding it belong to Ansible, Inc. They currently allow management up to 10 nodes for free, but anything beyond that requires a license. 
