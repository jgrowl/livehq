package tv.camfire.media.config

import redis.RedisClient
import tv.camfire.media.callback._

/**
 * User: jonathan
 * Date: 7/22/13
 * Time: 7:05 PM
 */
trait PublisherModule extends ClusterModule {
  lazy val redis = RedisClient(properties.redisHost, properties.redisPort)
  lazy val publisherCallback: PublisherCallback = wire[RedisPublisherCallback]
}
