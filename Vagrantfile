# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  vagrant_root = File.dirname(__FILE__)

  config.trigger.before :ALL do
    run "#{vagrant_root}/config/fill-templates.sh"
  end

  # config.vm.synced_folder "~/.bundle", "/usr/local/bundle", :create => true
  config.vm.synced_folder "~/.ivy2", "/home/app/.ivy2", :create => true
  config.vm.synced_folder "~/.pub-cache", "/home/app/.pub-cache", :create => true

  # config.vm.define "turnserver" do |turnserver|
  #   turnserver.vm.provider "docker" do |docker|
  #     docker.name  = "turnserver"
  #     docker.image = "bprodoehl/turnserver"
  #     docker.create_args = %w(-d --name=turnserver --restart="on-failure:10" --net=host)
  #     docker.ports = %w(3478:3478 3478:3478/udp)
  #     docker.vagrant_vagrantfile = __FILE__
  #     docker.remains_running = false
  #   end
  # end

  config.vm.define "consul" do |consul_server|
    consul_server.vm.provider "docker" do |docker|
      docker.name  = "consul"
      docker.image = "progrium/consul"
      docker.create_args = %w(-h node1)
      docker.cmd = ['-server', '-bootstrap', '-ui-dir','/ui']
      docker.ports = %w(8400:8400 8500:8500 8600:53/udp 172.17.42.1:53:53/udp)
      docker.vagrant_vagrantfile = __FILE__
      docker.remains_running = false
    end
  end

  config.vm.define "registrator" do |registrator|
    registrator.vm.provider "docker" do |docker|
      docker.name  = "registrator"
      docker.image = "gliderlabs/registrator:master"
      docker.create_args = %w(-d)
      docker.volumes = %w(/var/run/docker.sock:/tmp/docker.sock)
      docker.link "consul:consul"
      docker.cmd = ['-internal', 'consul://consul:8500']
      docker.vagrant_vagrantfile = __FILE__
      docker.remains_running = false
    end
  end

  config.vm.define "redis" do |redis|
    redis.vm.provider "docker" do |docker|
      docker.name  = "livehq_redis"
      docker.build_dir = "redis"
      docker.env = {
          'SERVICE_NAME' => 'livehq-redis'
      }
      docker.ports = ["6379:6379"]
      docker.vagrant_vagrantfile = __FILE__
      docker.remains_running = false
    end
  end

  config.vm.define "mongodb" do |redis|
    redis.vm.provider "docker" do |docker|
      docker.name  = "livehq_mongodb"
      docker.image = "mongo"
      docker.env = {
          'SERVICE_NAME' => 'livehq-mongodb'
      }
      docker.ports = ["27017:27017"]
      docker.vagrant_vagrantfile = __FILE__
      docker.remains_running = false
    end
  end

  config.vm.define "publisher" do |publisher|
    publisher.vm.provider "docker" do |docker|
      docker.name  = "livehq_publisher_vagrant"
      docker.build_dir = "media"
      docker.build_args = %W(-f #{vagrant_root}/media/DevDockerfile)
      docker.create_args = ["-w", "/vagrant/media", "--dns", "172.17.42.1",  "--dns-search", "service.consul"]
      docker.ports = ["2551:2551"]
      docker.env = {
          'SERVICE_NAME' => 'livehq-publisher-seed'
      }
      docker.cmd = ["sbt",
                    "-Djava.library.path=/vagrant/media/lib/native",
                    "-Dakka.remote.netty.tcp.hostname=livehq-publisher-seed",
                    "-Dakka.remote.netty.tcp.port=2551",
                    "run-main server.app.App publisher -p 2551"]
      docker.vagrant_vagrantfile = __FILE__
      docker.remains_running = false
    end
  end

  config.vm.define "publishermonitor" do |publisher|
    publisher.vm.provider "docker" do |docker|
      docker.name  = "livehq_publishermonitor_vagrant"
      docker.build_dir = "media"
      docker.build_args = %W(-f #{vagrant_root}/media/DevDockerfile)
      docker.create_args = ["-w", "/vagrant/media", "--dns", "172.17.42.1",  "--dns-search", "service.consul"]
      docker.ports = ["2552:2552"]
      docker.env = {
        'SERVICE_NAME' => 'livehq-publisher-monitor-seed',
      }
      docker.cmd = ["sbt",
                    "-Dakka.remote.netty.tcp.hostname=livehq-publisher-monitor-seed",
                    "-Dakka.remote.netty.tcp.port=2552",
                    "run-main server.app.App publisher-monitor -p 2552"]
      docker.vagrant_vagrantfile = __FILE__
      docker.remains_running = false
    end
  end

  config.vm.define "subscriber" do |subscriber|
    subscriber.vm.provider "docker" do |docker|
      docker.name  = "livehq_subscriber_vagrant"
      docker.build_dir = "media"
      docker.build_args = %W(-f #{vagrant_root}/media/DevDockerfile)
      docker.create_args = ["-w", "/vagrant/media", "--dns", "172.17.42.1",  "--dns-search", "service.consul"]
      docker.ports = ["2553:2553"]
      docker.env = {
        'SERVICE_NAME' => 'livehq-subscriber-seed'
      }
      docker.cmd = ["sbt",
                    "-Djava.library.path=/vagrant/media/lib/native",
                    "-Dakka.remote.netty.tcp.hostname=livehq-subscriber-seed",
                    "-Dakka.remote.netty.tcp.port=2553",
                    "run-main server.app.App subscriber -p 2553"]
      docker.vagrant_vagrantfile = __FILE__
      docker.remains_running = false
    end
  end

  config.vm.define "subscribermonitor" do |subscribermonitor|
    subscribermonitor.vm.provider "docker" do |docker|
      docker.name  = "livehq_subscribermonitor_vagrant"
      docker.build_dir = "media"
      docker.build_args = %W(-f #{vagrant_root}/media/DevDockerfile)
      docker.create_args = ["-w", "/vagrant/media", "--dns", "172.17.42.1",  "--dns-search", "service.consul"]
      docker.ports = ["2554:2554"]
      docker.env = {
        'SERVICE_NAME' => 'livehq-subscriber-monitor-seed',
      }
      docker.cmd = ["sbt",
                    "-Dakka.remote.netty.tcp.hostname=livehq-subscriber-monitor-seed",
                    "-Dakka.remote.netty.tcp.port=2554",
                    "run-main server.app.App subscriber-monitor -p 2554"]
      docker.vagrant_vagrantfile = __FILE__
      docker.remains_running = false
    end
  end

  config.vm.define "signal" do |signal|
    signal.vm.provider "docker" do |docker|
      docker.name  = "livehq_signal"
      docker.build_dir = "signal"
      docker.build_args = %W(-f #{vagrant_root}/signal/DevDockerfile)
      docker.create_args = ["-w", "/vagrant/signal", "--dns", "172.17.42.1",  "--dns-search", "service.consul"]
      docker.ports = ["1234:1234"]
      docker.env = {
          'SERVICE_NAME' => 'livehq-signal'
      }
      docker.vagrant_vagrantfile = __FILE__
      docker.remains_running = false
    end
  end

  config.vm.define "api" do |api|
    api.vm.provider "docker" do |docker|
      docker.name  = "livehq_api"
      docker.build_dir = "api"
      docker.build_args = %W(-f #{vagrant_root}/api/DevDockerfile)
      docker.create_args = ["-w", "/vagrant/api", "--dns", "172.17.42.1",  "--dns-search", "service.consul"]
      docker.ports = ["3000:3000"]
      docker.env = {
          'SERVICE_NAME' => 'livehq-api'
      }
      docker.vagrant_vagrantfile = __FILE__
      docker.remains_running = false
    end
  end

  # export PUB_CACHE=/home/app/.pub-cache
  config.vm.define "web" do |web|
    web.vm.provider "docker" do |docker|
      docker.name  = "livehq_web"
      docker.build_dir = "web"
      docker.build_args = %W(-f #{vagrant_root}/web/DevDockerfile)
      docker.create_args = ["-w", "/vagrant/web", "--dns", "172.17.42.1",  "--dns-search", "service.consul"]
      docker.ports = ["8080:8080"]
      docker.env = {
          'SERVICE_NAME' => 'livehq-web'
      }
      docker.vagrant_vagrantfile = __FILE__
      docker.remains_running = false
    end
  end

  config.push.define "test", strategy: "local-exec" do |push|
      push.script = "./config/push.sh"
  end

end
