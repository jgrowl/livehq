# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

ENV['VAGRANT_DEFAULT_PROVIDER'] = 'docker'

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|

  user_home = File.expand_path '~'
  vagrant_root = File.dirname(__FILE__)

  config.vm.synced_folder ".", "/vagrant", disabled: true

  if RUBY_PLATFORM =~ /darwin/
      config.vm.provider "docker" do |d|
        d.vagrant_vagrantfile = "HostVmVagrantfile"
      end
  else
  end

  #config.ssh.insert_key = false

  #config.vm.synced_folder ".", "/vagrant", type: "nfs"
  #config.vm.synced_folder "~/.ivy2", "/home/app/.ivy2", type: "nfs", create: true
  #config.vm.synced_folder "~/.pub-cache", "/home/app/.pub-cache", type: "nfs", create: true

   #config.vm.synced_folder "~/.ivy2", "/home/app/.ivy2", type: "nfs", create: true
   #config.vm.synced_folder "~/.pub-cache", "/home/app/.pub-cache", type: "nfs", create: true

  config.vm.define "consul-server" do |consul_server|
    consul_server.vm.provider "docker" do |docker|
      docker.name  = "livehq-consul-server"
      docker.image = "progrium/consul"
      docker.create_args = %w(-h node1)
      docker.cmd = ['-server', '-advertise', '172.17.0.1', '-bootstrap', '-ui-dir','/ui']
      docker.ports = %w(8400:8400 8500:8500 8600:53/udp)
      #docker.vagrant_vagrantfile = __FILE__
      docker.remains_running = false
    end
  end

  config.vm.define "registrator" do |registrator|
    registrator.vm.provider "docker" do |docker|
      docker.name  = "livehq-registrator"
      docker.image = "gliderlabs/registrator:master"
      docker.create_args = %w(-d)
      docker.volumes = %w(/var/run/docker.sock:/tmp/docker.sock)
      docker.link "livehq-consul-server:livehq-consul-server"
      docker.cmd = ['-internal', 'consul://livehq-consul-server:8500']
      #docker.vagrant_vagrantfile = __FILE__
      docker.remains_running = false
    end
  end

  config.vm.define "redis" do |redis|
    redis.vm.provider "docker" do |docker|
      docker.name  = "livehq-redis"
      #docker.create_args = ["-w", "/vagrant"]
      docker.build_dir = "./packer/redis"
      docker.env = {
          'SERVICE_NAME' => 'livehq-redis',
          # Just an example tag.
          # See http://progrium.com/blog/2014/09/10/automatic-docker-service-announcement-with-registrator/
          # 'SERVICE_TAGS' => 'primary'
      }
      docker.ports = ["6379:6379"]
      docker.volumes = %w(/vagrant/packer/redis:/vagrant)
      #docker.vagrant_vagrantfile = __FILE__
      docker.remains_running = false
    end
  end

  config.vm.define "publisher" do |publisher|
    publisher.vm.provider "docker" do |docker|
      docker.name  = "livehq-publisher"
      docker.image = "jgrowl/livehq-media:0.1"
      docker.link "livehq-consul-server:livehq-consul-server"
      docker.create_args = ["-w", "/vagrant", "--dns-search", "service.consul"]
      docker.ports = ["2551:2551"]
      docker.expose = ["2551"]
      docker.env = {
          'SERVICE_NAME' => 'livehq-publisher-seed',
          #'LD_LIBRARY_PATH' => '/vagrant/lib/native',
          'LD_LIBRARY_PATH' => '/vagrant/media/lib/native',
          'APP_UID' => Process.uid,
          'APP_GUID' => Process.gid
      }

      docker.volumes = %W(#{vagrant_root}:/vagrant)
      docker.cmd = %w(./media/packer/scripts/start-publisher.sh)
      #docker.vagrant_vagrantfile = __FILE__
      docker.remains_running = false
    end
  end

  config.vm.define "publisher-monitor" do |publisher_monitor|
    publisher_monitor.vm.provider "docker" do |docker|
      docker.name  = "livehq-publisher-monitor"
      docker.image = "jgrowl/livehq-media:0.1"
      docker.link "livehq-consul-server:livehq-consul-server"
      docker.create_args = ["-w", "/vagrant", "--dns-search", "service.consul"]
      docker.ports = ["2552:2552"]
      docker.expose = ["2552"]
      docker.env = {
          'SERVICE_NAME' => 'livehq-publisher-monitor-seed',
          #'LD_LIBRARY_PATH' => '/vagrant/lib/native',
          'LD_LIBRARY_PATH' => '/vagrant/media/lib/native',
          'APP_UID' => Process.uid,
          'APP_GUID' => Process.gid
      }
      docker.volumes = %W(#{vagrant_root}:/vagrant)
      docker.cmd = %w(./media/packer/scripts/start-publisher-monitor.sh)
      #docker.vagrant_vagrantfile = __FILE__
      docker.remains_running = false
    end
  end

  config.vm.define "subscriber" do |subscriber|
    subscriber.vm.provider "docker" do |docker|
      docker.name  = "livehq-subscriber"
      docker.image = "jgrowl/livehq-media:0.1"
      docker.link "livehq-consul-server:livehq-consul-server"
      docker.create_args = ["-w", "/vagrant", "--dns-search", "service.consul"]
      docker.ports = ["2553:2553"]
      docker.env = {
          'SERVICE_NAME' => 'livehq-subscriber-seed',
          #'LD_LIBRARY_PATH' => '/vagrant/lib/native',
          'LD_LIBRARY_PATH' => '/vagrant/media/lib/native',
          'APP_UID' => Process.uid,
          'APP_GUID' => Process.gid
      }

      docker.volumes = %W(#{vagrant_root}:/vagrant)
      docker.cmd = %w(./media/packer/scripts/start-subscriber.sh)
      #docker.vagrant_vagrantfile = __FILE__
      docker.remains_running = false
    end
  end

  config.vm.define "subscriber-monitor" do |subscriber_monitor|
    subscriber_monitor.vm.provider "docker" do |docker|
      docker.name  = "livehq-subscriber-monitor"
      docker.image = "jgrowl/livehq-media:0.1"
      docker.link "livehq-consul-server:livehq-consul-server"
      docker.create_args = ["-w", "/vagrant", "--dns-search", "service.consul"]
      docker.ports = ["2554:2554"]
      docker.env = {
          'SERVICE_NAME' => 'livehq-subscriber-monitor-seed',
          'LD_LIBRARY_PATH' => '/vagrant/media/lib/native',
          #'LD_LIBRARY_PATH' => '/vagrant/lib/native',
          'APP_UID' => Process.uid,
          'APP_GUID' => Process.gid
      }

      docker.volumes = %W(#{vagrant_root}:/vagrant)
      docker.cmd = %w(./media/packer/scripts/start-subscriber-monitor.sh)
      #docker.vagrant_vagrantfile = __FILE__
      docker.remains_running = false
    end
  end

  config.vm.define "signal" do |signal|
    signal.vm.provider "docker" do |docker|
      docker.name  = "livehq-signal"
      docker.image = "ruby:2.2-onbuild"
      docker.link "livehq-redis:livehq-redis"
      docker.link "livehq-consul-server:livehq-consul-server"
      docker.create_args = %w(-w /vagrant --dns-search service.consul)
      docker.ports = ["1234:1234"]
      docker.env = {
          'SERVICE_NAME' => 'livehq-signal',
          'APP_UID' => Process.uid,
          'APP_GUID' => Process.gid
      }
      docker.volumes = %W(#{vagrant_root}:/vagrant)
      docker.cmd = %w(./signal/scripts/start.sh)
      #docker.vagrant_vagrantfile = __FILE__
      docker.remains_running = false
    end
  end

  config.vm.define "api" do |api|
    api.vm.provider "docker" do |docker|
      docker.name  = "livehq-api"
      docker.image = "jgrowl/livehq-api:0.1"
      docker.link "livehq-consul-server:livehq-consul-server"
      docker.create_args = %w(-w /vagrant --dns-search service.consul)
      docker.ports = ["3000:3000"]
      docker.env = {
          'SERVICE_NAME' => 'livehq-api',
          'RBENV_ROOT' => '/usr/local/rbenv',
          'APP_UID' => Process.uid,
          'APP_GUID' => Process.gid
      }
      docker.volumes = %W(#{vagrant_root}:/vagrant)
      docker.cmd = %w(./api/packer/scripts/start.sh)
      #docker.vagrant_vagrantfile = __FILE__
      docker.remains_running = false
    end
  end

  config.vm.define "web" do |web|
    web.vm.provider "docker" do |docker|
      docker.name  = "livehq-web"
      docker.image = "jgrowl/livehq-web:0.1"
      docker.link "livehq-consul-server:livehq-consul-server"
      docker.ports = ["8080:8080"]
      docker.create_args = %w(-w /vagrant/signal/client/example --dns-search service.consul)
      docker.env = {
          'SERVICE_NAME' => 'livehq-web',
          'APP_UID' => Process.uid,
          'APP_GUID' => Process.gid,
          'PUB_CACHE' => '/vagrant/.pub-cache'
      }
      docker.volumes = %W(#{vagrant_root}:/vagrant)
      docker.cmd = %w(./packer/scripts/start.sh)
      #docker.vagrant_vagrantfile = __FILE__
      docker.remains_running = false
    end
  end
end
