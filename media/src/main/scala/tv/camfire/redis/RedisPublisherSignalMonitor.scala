package tv.camfire.redis

import java.net.InetSocketAddress

import akka.actor.ActorLogging
import akka.contrib.pattern.ClusterSharding
import org.json4s.jackson
import org.webrtc.{IceCandidate, SessionDescription}
import redis.actors.RedisSubscriberActor
import redis.api.pubsub.{Message, PMessage}
import server.Publisher
import tv.camfire.media.config.Properties
import tv.camfire.webrtc.serialization.jackson.WebrtcSerializationSupport

/**
 * Created by jonathan on 10/4/14.
 */
class RedisPublisherSignalMonitor(channels: Seq[String] = Nil, patterns: Seq[String] = Nil, properties: Properties)
  extends RedisSubscriberActor(new InetSocketAddress(properties.redisHost, properties.redisPort), channels, patterns) with jackson.JsonMethods
  with WebrtcSerializationSupport with ActorLogging {

  val publisherRegion = ClusterSharding(context.system).shardRegion(Publisher.shardName)

  def onMessage(message: Message) {
    log.debug(s" message received: $message")
  }

  def onPMessage(pmessage: PMessage) {
    val pchannel = pmessage.channel
    log.debug(s"pattern message received: $pmessage")
    val split = pchannel.split(":")
    val channel = split(0)
    val identifier = split(1)
    val data = pmessage.data

    channel match {
      case "media.publisher.webrtc.offer" =>
        val s: SessionDescription = mapper.readValue(data, classOf[SessionDescription])
        publisherRegion ! Publisher.Offer(identifier, s)
      case "media.publisher.webrtc.ice-candidate" =>
        val c: IceCandidate = mapper.readValue(data, classOf[IceCandidate])
        publisherRegion ! Publisher.Candidate(identifier, c)
      case _ =>
        log.warning(s"Unhandled message [$channel]")
    }
  }
}
