package tv.camfire.media.config

import akka.actor.ActorSystem
import com.softwaremill.macwire.Macwire
import com.typesafe.config.ConfigFactory
import org.webrtc.{PeerConnection, PeerConnectionFactory}
import redis.RedisClient
import tv.camfire.media.callback._
import tv.camfire.media.webrtc.WebRtcHelper

/**
 * User: jonathan
 * Date: 7/22/13
 * Time: 7:05 PM
 */
trait ClusterModule extends Macwire {
  /**
   * Utilities & Configuration
   */
  lazy val properties: Properties = new Properties {}

  def port(): String

  // Override the configuration of the port
  val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
    withFallback(ConfigFactory.load())

  lazy implicit val actorSystem = ActorSystem("ClusterSystem", config)

  /**
   * WebRTC
   */
  lazy val iceServers = new java.util.ArrayList[PeerConnection.IceServer]()
  iceServers.add(new PeerConnection.IceServer(properties.iceUri, properties.iceUsername, properties.icePassword))

  lazy val peerConnectionFactory = wire[PeerConnectionFactory]
  lazy val webRtcHelper: WebRtcHelper = wire[WebRtcHelper]

}
