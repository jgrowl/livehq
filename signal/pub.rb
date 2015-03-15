# usage:
# ruby pub.rb channel username

require 'rubygems'
require 'redis'
require 'json'

# $redis = Redis.new
$redis = Redis.new(:host => "livehq-redis")

channel = 'signal'   #ARGV[0]
origin = '1'
destination = '1'
msg = {'type' => 'offer'}

# data = {"user" => ARGV[1]}
data = {'origin' => origin, 'destination' => destination, 'msg' => msg}

loop do
  msg = STDIN.gets
  $redis.publish channel, data.to_json
end
