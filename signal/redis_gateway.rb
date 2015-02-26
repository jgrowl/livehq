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
    subscribe('register_identifier', :register_identifier)
  end

  def publish_on_redis(topic, channel, data)
    @redis.publish channel, data
  end

  def register_identifier(topic, identifier, origin)
    @redis.set("origin:#{identifier}", origin)
  end

  def socket_created(topic, id, socket)
    info "Registering socket: [#{id}]"
    @sockets[id] = socket
  end

  def socket_closed(topic, id)
    @sockets.delete(id)
  end

  def resolve_origin(identifier)
    origin = @redis.get("origin:#{identifier}")
    warn 'HITHERE'
    warn origin
    origin
  end

  def run
    defer {
      redis = ::Redis.new(:timeout => 0, :driver => :celluloid)
      redis.psubscribe("web.*") do |on|
        on.pmessage do |pattern, channel, message|
          split = channel.split(':')
          type = split[0]
          identifier = split[1]
          origin = resolve_origin(identifier)

          data = JSON.parse(message)
          case type
            when 'web.publisher.webrtc.answer'
              send_if_origin(origin, wrap_message(identifier, 'answer', data))
            when 'web.publisher.webrtc.offer'
              send_if_origin(origin, wrap_message(identifier, 'offer', data))
            when 'web.publisher.webrtc.ice-candidate'
              send_if_origin(origin, wrap_message(identifier, 'ice-candidate', data))

            when 'web.subscriber.webrtc.answer'
              send_if_origin(origin, wrap_message(identifier, type, data))
            when 'web.subscriber.webrtc.offer'
              send_if_origin(origin, wrap_message(identifier, type, data))
            when 'web.subscriber.webrtc.ice-candidate'
              send_if_origin(origin, wrap_message(identifier, type, data))
            else
              warn "Unhandled message #{type}"
          end
        end
      end
    }
  rescue Redis::CannotConnectError => e
    error e.message
  end

  def wrap_message(identifier, type, data)
    JSON.generate({:identifier => identifier, :type => type, :data => data})
  end

  def send_if_origin(origin, data)
    if @sockets.key?(origin)
      @sockets[origin] << data
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

