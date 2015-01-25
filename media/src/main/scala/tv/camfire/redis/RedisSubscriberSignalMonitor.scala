package tv.camfire.redis

import java.net.InetSocketAddress

import akka.actor.ActorLogging
import akka.contrib.pattern.ClusterSharding
import org.json4s.jackson
import org.webrtc.{IceCandidate, SessionDescription}
import redis.actors.RedisSubscriberActor
import redis.api.pubsub.{Message, PMessage}
import server.Subscriber
import tv.camfire.webrtc.serialization.jackson.WebrtcSerializationSupport

/**
 * Created by jonathan on 10/4/14.
 */
class RedisSubscriberSignalMonitor(channels: Seq[String] = Nil, patterns: Seq[String] = Nil)
  extends RedisSubscriberActor(new InetSocketAddress("localhost", 6379), channels, patterns) with jackson.JsonMethods
  with WebrtcSerializationSupport with ActorLogging {

  val subscriberRegion = ClusterSharding(context.system).shardRegion(Subscriber.shardName)

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
      case "media.subscriber.webrtc.offer" =>
        val s: SessionDescription = mapper.readValue(data, classOf[SessionDescription])
        subscriberRegion ! Subscriber.Offer(origin, s)
      case "media.subscriber.webrtc.ice-candidate" =>
        val c: IceCandidate = mapper.readValue(data, classOf[IceCandidate])
        subscriberRegion ! Subscriber.Candidate(origin, c)
      case "media.subscriber.webrtc.subscribe" =>
        // TODO: parse off target identifier
        log.info("Subscribing [{}] to identifier: [{}]", origin, 1)
        subscriberRegion ! Subscriber.Subscribe(origin, "1")
      case _ =>
        log.warning("Received unknown message!");
    }
  }
}
