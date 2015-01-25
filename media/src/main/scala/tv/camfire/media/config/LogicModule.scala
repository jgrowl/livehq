package tv.camfire.media.config

import akka.actor.ActorSystem
import com.softwaremill.macwire.Macwire
import com.typesafe.config.ConfigFactory
import org.webrtc.PeerConnection
import redis.RedisClient
import tv.camfire.media.callback.{RedisSubscriberCallback, SubscriberCallback, Callback, RedisCallback}
import tv.camfire.media.factory.CamfirePeerConnectionFactory
import tv.camfire.media.webrtc.WebRtcHelper

/**
 * User: jonathan
 * Date: 7/22/13
 * Time: 7:05 PM
 */
trait LogicModule extends Macwire {
  /**
   * Utilities & Configuration
   */
  lazy val properties: Properties = new Properties {}

  def port(): String

  // Override the configuration of the port
  val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
    withFallback(ConfigFactory.load())

  lazy implicit val actorSystem = ActorSystem("ClusterSystem", config)

//  val channels = Seq("time")
//  val patterns = Seq("pattern.*")
//  akkaSystem.actorOf(Props(classOf[SubscribeActor], channels, patterns).withDispatcher("rediscala.rediscala-client-worker-dispatcher"))

  /**
   * Server
   */
  //  lazy val callback: Callback = TypedActor(actorSystem).typedActorOf(TypedProps[DbCallback])
//  lazy val callback: Callback = wire[RestCallback]
  lazy val redis = RedisClient()
  lazy val callback: Callback = wire[RedisCallback]
  lazy val subscriberCallback: SubscriberCallback = wire[RedisSubscriberCallback]

//  val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
//    withFallback(ConfigFactory.load())
//

  /**
   * WebRTC
   */
  lazy val iceServers = new java.util.ArrayList[PeerConnection.IceServer]()
  iceServers.add(new PeerConnection.IceServer(properties.iceUri, properties.iceUsername, properties.icePassword))

  lazy val camfirePeerConnectionFactory = wire[CamfirePeerConnectionFactory]
  lazy val webRtcHelper: WebRtcHelper = wire[WebRtcHelper]

}
