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
    # @id = SecureRandom.hex(10)
    @id = '1'
    info 'Streaming changes to client'
    @socket = websocket
    @socket.on_message do |msg|
      info "message: #{msg}"
      handle_message(msg)
    end

    @socket.on_close do |msg|
      info "Socket closed [#{@id}]"
      publish :socket_closed, @id
    end

    publish 'socket_created', @id, @socket
    async.run
  end

  def handle_message(msg)
    json = JSON.parse(msg)
    type = json['type']
    data = JSON.generate(json['data'])
    case type
      when 'media.webrtc.create-peerconnection'
        publish 'publish_on_redis', "#{type}:#{@id}", data
      when 'media.webrtc.offer'
        publish 'publish_on_redis', "#{type}:#{@id}", data
      else
        warn "Unknown message: [#{type}]"
    end
  end

  def run
    every(5) do
      @socket.read
    end
  rescue Reel::SocketError, EOFError
    info "WS client disconnected"
    terminate
  end
end

