package tv.camfire.redis

import java.net.InetSocketAddress

import akka.contrib.pattern.ClusterSharding
import org.json4s.jackson
import org.webrtc.SessionDescription
import redis.actors.RedisSubscriberActor
import redis.api.pubsub.{PMessage, Message}
import server.Publisher
import tv.camfire.webrtc.serialization.jackson.WebrtcSerializationSupport

/**
 * Created by jonathan on 10/4/14.
 */
class SubscribeActor(channels: Seq[String] = Nil, patterns: Seq[String] = Nil)
  extends RedisSubscriberActor(new InetSocketAddress("localhost", 6379), channels, patterns) with jackson.JsonMethods
  with WebrtcSerializationSupport {

  val connectionRegion = ClusterSharding(context.system).shardRegion(Publisher.shardName)

  def onMessage(message: Message) {
    println(s" message received: $message")
  }

  def onPMessage(pmessage: PMessage) {
    val pchannel = pmessage.channel
    val split = pchannel.split(":")
    val channel = split(0)

//    val origin = if(split.length > 1) "" else split(1)
    val origin = "1"
    val data = pmessage.data
    channel match {
      case "media.webrtc.create-peerconnection" =>
        connectionRegion ! Publisher.Incoming.AddPeerConnection(origin)
      case "media.webrtc.offer" =>
        val s: SessionDescription = mapper.readValue(data, classOf[SessionDescription])
        connectionRegion ! Publisher.Incoming.Offer(origin, s)
      case _ =>
        println("No match")
    }

    println(s"pattern message received: $pmessage")
  }
}
