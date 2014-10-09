require 'rubygems'
require 'bundler/setup'
require 'reel'
require 'celluloid/autostart'
require_relative 'redis_gateway'
require_relative 'websocket_client'

class WebServer < Reel::Server::HTTP
  include Celluloid::Logger
  include Celluloid::Notifications

  def initialize(host = "127.0.0.1", port = 1234)
    info "LiveHQ WebSocket server starting on #{host}:#{port}"
    super(host, port, &method(:on_connection))
  end

  def on_connection(connection)
    while request = connection.request
      if request.websocket?
        info 'Received a WebSocket connection'
        connection.detach
        route_websocket request.websocket
        return
      else
        info "404 Not Found: #{request.path}"
        connection.respond :not_found, "Not found"
      end
    end
  end

  def route_websocket(socket)
    if socket.url == "/ws"
      WebsocketClient.new(socket)
    else
      info "Received invalid WebSocket request for: #{socket.url}"
      socket.close
    end
  end

end

RedisGateway.supervise_as :redis_client
WebServer.supervise_as :reel

sleep
