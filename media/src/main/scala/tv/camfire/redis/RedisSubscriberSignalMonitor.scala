package tv.camfire.redis

import java.net.InetSocketAddress

import akka.actor.ActorLogging
import akka.contrib.pattern.ClusterSharding
import org.json4s.{DefaultFormats, jackson}
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
      case "media.subscriber.webrtc.answer" =>
        val s: SessionDescription = mapper.readValue(data, classOf[SessionDescription])
        subscriberRegion ! Subscriber.Answer(identifier, s)
      case "media.subscriber.webrtc.ice-candidate" =>
        val c: IceCandidate = mapper.readValue(data, classOf[IceCandidate])
        subscriberRegion ! Subscriber.Candidate(identifier, c)
      case "media.subscriber.webrtc.subscribe" =>
        // TODO: Instead of passing the publisherIdentifier here, maybe it would be better to bind a subscriberIdentifier
        // to a specific publisher when it is created.
        implicit val formats = DefaultFormats
        val dataMap = parse(data).extract[Map[String, String]]
        val publisherIdentifier = dataMap.get("publisherIdentifier").get
        log.info("Subscribing [{}] to publisher: [{}]", identifier, publisherIdentifier)
        subscriberRegion ! Subscriber.Subscribe(identifier, publisherIdentifier)
      case _ =>
        log.warning("Received unknown message!");
    }
  }
}
