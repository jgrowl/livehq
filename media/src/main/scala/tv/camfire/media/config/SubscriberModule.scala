package tv.camfire.media.config

import redis.RedisClient
import tv.camfire.media.callback._

/**
 * User: jonathan
 * Date: 7/22/13
 * Time: 7:05 PM
 */
trait SubscriberModule extends ClusterModule {
  /**
   * Server
   */
  lazy val redis = RedisClient(properties.redisHost, properties.redisPort)
  lazy val subscriberCallback: SubscriberCallback = wire[RedisSubscriberCallback]
}
