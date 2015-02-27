require 'rubygems'
require 'bundler/setup'
require 'reel'
require 'celluloid/autostart'
require 'redis'
require 'json'
require_relative 'redis_gateway'

class WebsocketClient
  include Celluloid
  include Celluloid::Notifications
  include Celluloid::Logger

  def initialize(websocket)
    @id = SecureRandom.hex(10)
    info "Streaming changes to client(#{@id})"
    @socket = websocket
    @socket.on_message do |msg|
      debug "message: #{msg}"
      handle_message(msg)
    end

    @socket.on_close do |msg|
      info "Socket closed [#{@id}]"
      publish :socket_closed, @id
    end

    publish 'socket_created', @id, @socket
    async.run
  end

  def missing_keys(hash)
    !(hash.has_key('type') && hash.has_key('identifier'))
  end

  def handle_message(msg)
    json = JSON.parse(msg)
    type = json['type']
    identifier = json['identifier']
    # TODO: It might be wise to log something if there are missing keys!
    return if type.nil? || identifier.nil?
    data = JSON.generate(json['data'])
    case type
      when 'media.publisher.webrtc.offer'
        # Doing this might cause a race condition. I might need to ensure that this completes before I publish
        # the offer.
        publish 'register_identifier', identifier, @id
        publish 'publish_on_redis', "#{type}:#{identifier}", data
      when 'media.publisher.webrtc.ice-candidate'
        publish 'register_identifier', identifier, @id
        publish 'publish_on_redis', "#{type}:#{identifier}", data
      when 'web.subscriber.webrtc.offer'
        publish 'publish_on_redis', "#{type}:#{identifier}", data
      when 'web.subscriber.webrtc.ice-candidate'
        publish 'register_identifier', identifier, @id
      when 'web.subscriber.webrtc.answer'
        publish 'publish_on_redis', "#{type}:#{identifier}", data

      when 'media.subscriber.webrtc.subscribe'
        publish 'register_identifier', identifier, @id
        publish 'publish_on_redis', "#{type}:#{identifier}", data
      when 'media.subscriber.webrtc.answer'
        publish 'register_identifier', identifier, @id
        publish 'publish_on_redis', "#{type}:#{identifier}", data
      when 'media.subscriber.webrtc.ice-candidate'
        publish 'register_identifier', identifier, @id
        publish 'publish_on_redis', "#{type}:#{identifier}", data

      else
        warn "Unknown message: [#{type}]"
    end

  rescue JSON::ParserError
    warn 'Problem parsing...'
  end

  def run
    @socket.read_every 0.1
    rescue Reel::SocketError, EOFError
      info "WS client disconnected"
      terminate
  end
end

