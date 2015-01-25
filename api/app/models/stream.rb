require 'redis'

class Stream
  def self.all
    redis = Redis.new
    pcs = redis.smembers 'pcs'

    {pcs: pcs.map { |pc| {
        id: pc,
        streams: redis.smembers("pc:#{pc}:streams"),
        registries: redis.smembers("pc:#{pc}:rs").map { |r|
          {id: r }.merge(redis.hgetall("pc:#{pc}:r:#{r}"))
        },
        subscribers: redis.smembers("pc:#{pc}:subscribers")
    }.merge(redis.hgetall("pc:#{pc}"))}}
  end
end
