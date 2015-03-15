# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|

  config.vm.provider "docker"

  config.vm.define "redis" do |media|
    media.vm.provider "docker" do |docker|
      docker.build_dir = "./packer/redis"
      docker.name  = "livehq-redis"
      docker.ports = ["6379:6379"]
      docker.expose = ["6379"]
      docker.vagrant_vagrantfile = __FILE__
      docker.remains_running = false
    end
  end

  config.vm.define "signal" do |media|
    media.vm.provider "docker" do |docker|
      docker.name  = "livehq-signal"
      docker.build_dir = "./signal"
      docker.expose = ["1234"]
      docker.ports = ["1234:1234"]
      docker.link "livehq-redis:livehq-redis"
      docker.vagrant_vagrantfile = __FILE__
      docker.remains_running = false
    end
  end

  config.vm.define "api" do |api|
    api.vm.provider "docker" do |docker|
      docker.name  = "livehq-api"
      docker.image = "livehq/api:0.1"
      docker.link "livehq-redis:livehq-redis"
      docker.env = {
          'RBENV_ROOT' => '/usr/local/rbenv',
          'PATH' => '/usr/local/rbenv/shims:/usr/local/rbenv/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'
      }
      docker.create_args = %w(-w /vagrant/api)
      docker.expose = ["3000"]
      docker.ports = ["3000:3000"]
      docker.vagrant_vagrantfile = __FILE__
      docker.remains_running = false
      docker.cmd = %w(./packer/scripts/start.sh)
    end
  end

  config.vm.define "media" do |media|
    media.vm.provider "docker" do |docker|
      docker.name  = "livehq-media"
      docker.image = "livehq/media:0.1"
      docker.link "livehq-redis:livehq-redis"
      docker.create_args = %w(-w /vagrant/media)
      docker.cmd = %w(./packer/scripts/start.sh)
      docker.vagrant_vagrantfile = __FILE__
      docker.remains_running = false
    end
  end

  config.vm.define "web" do |media|
    media.vm.provider "docker" do |docker|
      docker.name  = "livehq-web"
      docker.image = "livehq/web:0.1"
      docker.expose = ["8080"]
      docker.ports = ["8080:8080"]
      # docker.create_args = %w(-w /vagrant/web)
      docker.create_args = %w(-w /vagrant/signal/client/example)
      docker.env = {
          'PATH' => '/usr/lib/dart/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'
      }
      docker.cmd = %w(./packer/scripts/start.sh)
      docker.vagrant_vagrantfile = __FILE__
      docker.remains_running = false
    end
  end

  # config.vm.define "default", autostart: false do |default|
  # config.vm.define "default" do |default|
  #   default.vm.box = "yungsang/boot2docker"
  #   default.vm.network "private_network", ip: "192.168.33.10"
  #   default.vm.synced_folder ".", "/var/www", type: "nfs"
  #
  #   default.vm.provider "virtualbox" do |virtualbox|
  #     virtualbox.memory = 2048
  #   end
  #
  #   default.vm.provision "docker" do |docker|
  #     docker.build_image "/var/www/config/environment/docker/media", args: "-t vagrantdocker/media"
  #     # docker.build_image "/var/www/config/environment/docker/phpfpm", args: "-t vagrantdocker/phpfpm"
  #   end
  #
  #   default.ssh.insert_key = false
  #   default.ssh.username = "docker"
  #   default.ssh.password = "tcuser"
  # end

  # Every Vagrant virtual environment requires a box to build off of.
  # config.vm.box = "base"

  # Disable automatic box update checking. If you disable this, then
  # boxes will only be checked for updates when the user runs
  # `vagrant box outdated`. This is not recommended.
  # config.vm.box_check_update = false

  # Create a forwarded port mapping which allows access to a specific port
  # within the machine from a port on the host machine. In the example below,
  # accessing "localhost:8080" will access port 80 on the guest machine.
  # config.vm.network "forwarded_port", guest: 80, host: 8080

  # Create a private network, which allows host-only access to the machine
  # using a specific IP.
  # config.vm.network "private_network", ip: "192.168.33.10"

  # Create a public network, which generally matched to bridged network.
  # Bridged networks make the machine appear as another physical device on
  # your network.
  # config.vm.network "public_network"

  # If true, then any SSH connections made will enable agent forwarding.
  # Default value: false
  # config.ssh.forward_agent = true

  # Share an additional folder to the guest VM. The first argument is
  # the path on the host to the actual folder. The second argument is
  # the path on the guest to mount the folder. And the optional third
  # argument is a set of non-required options.
  # config.vm.synced_folder "../data", "/vagrant_data"

  # Provider-specific configuration so you can fine-tune various
  # backing providers for Vagrant. These expose provider-specific options.
  # Example for VirtualBox:
  #
  # config.vm.provider "virtualbox" do |vb|
  #   # Don't boot with headless mode
  #   vb.gui = true
  #
  #   # Use VBoxManage to customize the VM. For example to change memory:
  #   vb.customize ["modifyvm", :id, "--memory", "1024"]
  # end
  #
  # View the documentation for the provider you're using for more
  # information on available options.
  # config.vm.provider "docker" do |d|
  #   d.image = "ubuntu:14.04.2"
  # end

  # Enable provisioning with CFEngine. CFEngine Community packages are
  # automatically installed. For example, configure the host as a
  # policy server and optionally a policy file to run:
  #
  # config.vm.provision "cfengine" do |cf|
  #   cf.am_policy_hub = true
  #   # cf.run_file = "motd.cf"
  # end
  #
  # You can also configure and bootstrap a client to an existing
  # policy server:
  #
  # config.vm.provision "cfengine" do |cf|
  #   cf.policy_server_address = "10.0.2.15"
  # end

  # Enable provisioning with Puppet stand alone.  Puppet manifests
  # are contained in a directory path relative to this Vagrantfile.
  # You will need to create the manifests directory and a manifest in
  # the file default.pp in the manifests_path directory.
  #
  # config.vm.provision "puppet" do |puppet|
  #   puppet.manifests_path = "manifests"
  #   puppet.manifest_file  = "default.pp"
  # end

  # Enable provisioning with chef solo, specifying a cookbooks path, roles
  # path, and data_bags path (all relative to this Vagrantfile), and adding
  # some recipes and/or roles.
  #
  # config.vm.provision "chef_solo" do |chef|
  #   chef.cookbooks_path = "../my-recipes/cookbooks"
  #   chef.roles_path = "../my-recipes/roles"
  #   chef.data_bags_path = "../my-recipes/data_bags"
  #   chef.add_recipe "mysql"
  #   chef.add_role "web"
  #
  #   # You may also specify custom JSON attributes:
  #   chef.json = { mysql_password: "foo" }
  # end

  # Enable provisioning with chef server, specifying the chef server URL,
  # and the path to the validation key (relative to this Vagrantfile).
  #
  # The Opscode Platform uses HTTPS. Substitute your organization for
  # ORGNAME in the URL and validation key.
  #
  # If you have your own Chef Server, use the appropriate URL, which may be
  # HTTP instead of HTTPS depending on your configuration. Also change the
  # validation key to validation.pem.
  #
  # config.vm.provision "chef_client" do |chef|
  #   chef.chef_server_url = "https://api.opscode.com/organizations/ORGNAME"
  #   chef.validation_key_path = "ORGNAME-validator.pem"
  # end
  #
  # If you're using the Opscode platform, your validator client is
  # ORGNAME-validator, replacing ORGNAME with your organization name.
  #
  # If you have your own Chef Server, the default validation client name is
  # chef-validator, unless you changed the configuration.
  #
  #   chef.validation_client_name = "ORGNAME-validator"
end