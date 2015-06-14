package tv.camfire.media.config

import org.webrtc.{PeerConnection, PeerConnectionFactory}
import redis.RedisClient
import tv.camfire.media.callback.{RedisSubscriberCallback, SubscriberCallback, RedisPublisherCallback, PublisherCallback}
import tv.camfire.media.webrtc.WebRtcHelper

/**
 * User: jonathan
 * Date: 7/22/13
 * Time: 7:05 PM
 */
trait ClusterModule extends BaseModule {

  lazy val redis = RedisClient(properties.redisHost, properties.redisPort)
  lazy val publisherCallback: PublisherCallback = wire[RedisPublisherCallback]
  lazy val subscriberCallback: SubscriberCallback = wire[RedisSubscriberCallback]

  /**
   * WebRTC
   */
  lazy val iceServers: java.util.List[PeerConnection.IceServer] = new java.util.LinkedList[PeerConnection.IceServer]()
  iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"))
  iceServers.add(new PeerConnection.IceServer("stun:stun1.l.google.com:19302"))
  iceServers.add(new PeerConnection.IceServer("stun:stun2.l.google.com:19302"))
  iceServers.add(new PeerConnection.IceServer("stun:stun3.l.google.com:19302"))
  iceServers.add(new PeerConnection.IceServer("stun:stun4.l.google.com:19302"))


  lazy val peerConnectionFactory = wire[PeerConnectionFactory]
  lazy val webRtcHelper: WebRtcHelper = wire[WebRtcHelper]

}
