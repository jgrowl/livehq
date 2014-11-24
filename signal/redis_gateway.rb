require 'rubygems'
require 'bundler/setup'
require 'celluloid/autostart'
require 'redis'
require 'json'

class RedisGateway
  include Celluloid
  include Celluloid::Notifications
  include Celluloid::Logger

  def initialize
    @sockets = {}
    @redis = ::Redis.new(:timeout => 0, :driver => :celluloid)
    init_subscriptions
    async.run
  end

  def init_subscriptions
    subscribe('socket_created', :socket_created)
    subscribe('socket_closed', :socket_closed)
    subscribe('publish_on_redis', :publish_on_redis)
  end

  def publish_on_redis(topic, channel, data)
    @redis.publish channel, data
  end

  def socket_created(topic, id, socket)
    info "Registering socket: [#{id}]"
    @sockets[id] = socket
  end

  def socket_closed(topic, id)
    @sockets.delete(id)
  end

  def run
    defer {
      redis = ::Redis.new(:timeout => 0, :driver => :celluloid)
      redis.psubscribe("web.*") do |on|
        on.pmessage do |pattern, channel, message|
          split = channel.split(':')
          type = split[0]
          identifier = split[1]
          data = JSON.parse(message)
          case type
            when 'web.webrtc.answer'
              send_if_identifier(identifier, wrap_message('answer', data))
            when 'web.webrtc.offer'
              send_if_identifier(identifier, wrap_message('offer', data))
            when 'web.webrtc.ice-candidate'
              send_if_identifier(identifier, wrap_message('ice-candidate', data))
            else
          end
        end
      end
    }
  rescue Redis::CannotConnectError => e
    error e.message
  end

  def wrap_message(type, data)
    JSON.generate({:type => type, :data => data})
  end

  def send_if_identifier(identifier, data)
    if @sockets.key?(identifier)
      @sockets[identifier] << data
    end

  end
  #
  # def handle(channel, msg)
  #   data = JSON.parse(msg)
  #   origin = data['origin']
  #   destination = data['destination']
  #   message = data['msg']
  #   if @sockets.key?(destination)
  #     info "##{channel} - [#{origin}] -> [#{destination}]: #{message}"
  #     @sockets[destination] << message.to_json
  #   end
  # rescue StandardError => e
  #   warn e.message
  # end
end

