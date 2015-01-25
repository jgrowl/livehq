package tv.camfire.redis

import java.net.InetSocketAddress

import akka.actor.ActorLogging
import akka.contrib.pattern.ClusterSharding
import org.json4s.jackson
import org.webrtc.{IceCandidate, SessionDescription}
import redis.actors.RedisSubscriberActor
import redis.api.pubsub.{Message, PMessage}
import server.Publisher
import tv.camfire.webrtc.serialization.jackson.WebrtcSerializationSupport

/**
 * Created by jonathan on 10/4/14.
 */
class RedisPublisherSignalMonitor(channels: Seq[String] = Nil, patterns: Seq[String] = Nil)
  extends RedisSubscriberActor(new InetSocketAddress("localhost", 6379), channels, patterns) with jackson.JsonMethods
  with WebrtcSerializationSupport with ActorLogging {

  val publisherRegion = ClusterSharding(context.system).shardRegion(Publisher.shardName)

  def onMessage(message: Message) {
    println(s" message received: $message")
  }

  def onPMessage(pmessage: PMessage) {
    val pchannel = pmessage.channel
    log.info(s"pattern message received: $pmessage")
    val split = pchannel.split(":")
    val channel = split(0)

    // TODO: Change this back. Only using 1 as origin as means of testing
//    val origin = if(split.length > 1) "" else split(1)
    val origin = "1"
    val data = pmessage.data

    channel match {
      case "media.webrtc.offer" =>
        val s: SessionDescription = mapper.readValue(data, classOf[SessionDescription])
        publisherRegion ! Publisher.Offer(origin, s)
      case "media.webrtc.ice-candidate" =>
        val c: IceCandidate = mapper.readValue(data, classOf[IceCandidate])
        publisherRegion ! Publisher.Candidate(origin, c)
      case _ =>
        log.info("not fore me")
    }
  }
}
