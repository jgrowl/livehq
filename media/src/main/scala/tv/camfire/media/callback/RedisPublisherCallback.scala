package tv.camfire.media.callback

import org.json4s.jackson
import org.slf4j.LoggerFactory
import org.webrtc.PeerConnection.{IceConnectionState, IceGatheringState, SignalingState}
import org.webrtc.{IceCandidate, MediaStream, SessionDescription}
import redis.RedisClient
import tv.camfire.media.config.Properties
import tv.camfire.webrtc.serialization.jackson.WebrtcSerializationSupport


/**
 * Created by jonathan on 12/15/13.
 */
class RedisPublisherCallback(properties: Properties, redis: RedisClient)() extends PublisherCallback with jackson.JsonMethods
with WebrtcSerializationSupport {
  val log = LoggerFactory.getLogger(getClass)

  def pcsId: String= {
    "pcs"
  }

  def pcId(identifier: String): String = {
    s"pc:$identifier"
  }

  def registryPcsId(identifier: String): String = {
    s"${pcId(identifier)}:rs"
  }

  def registryPcId(identifier: String, uuid: String): String = {
    s"${pcId(identifier)}:r:$uuid"
  }

  def pcStreamsId(identifier: String): String = {
    s"${pcId(identifier)}:streams"
  }
  override def sendAnswer(identifier: String, answer: SessionDescription): Unit = {
    redis.publish("web.publisher.webrtc.answer:%s".format(identifier), mapper.writeValueAsString(answer))
  }

  override def sendIceCandidate(identifier: String, iceCandidate: IceCandidate): Unit = {
    redis.publish("web.publisher.webrtc.ice-candidate:%s".format(identifier), mapper.writeValueAsString(iceCandidate))
  }

  override def onAddStream(identifier: String, mediaStream: MediaStream): Unit = {
    redis.sadd(pcStreamsId(identifier), mediaStream.label())
  }

  override def onRemoveStream(identifier: String, mediaStream: MediaStream): Unit = {
    redis.srem(pcStreamsId(identifier), mediaStream.label())
  }

  override def onIceConnectionChange(identifier: String, iceConnectionState: IceConnectionState): Unit = {
    if (iceConnectionState == IceConnectionState.DISCONNECTED || iceConnectionState == IceConnectionState.CLOSED) {
      redis.srem(pcsId, identifier)
      // Ripley: I say we take off and nuke the entire site from orbit. It's the only way to be sure.
      redis.eval("return redis.call('del', unpack(redis.call('keys', ARGV[1])))",  Seq("0"), Seq(s"${pcId(identifier)}*"))
      return
    } else if (iceConnectionState == IceConnectionState.CONNECTED) {
      redis.sadd(pcsId, identifier)
    }

    redis.hset(pcId(identifier), "ice-connection-state", iceConnectionState.name())
  }

  override def onIceGatheringChange(identifier: String, iceGatheringState: IceGatheringState): Unit = {
    redis.hset(pcId(identifier), "ice-connection-state", iceGatheringState.name())
  }

  override def onSignalingChange(identifier: String, signalState: SignalingState): Unit = {
    redis.hset(pcId(identifier), "signal-state", signalState.name())
  }

  override def onRegistryPubInitialize(identifier: String, uuid: String, path: String): Unit = {
    redis.sadd(registryPcsId(identifier), uuid)
    redis.hset(registryPcId(identifier, uuid), "pub-path", path)
  }

  override def onRegistryPubIceConnectionChange(identifier: String, uuid: String, iceConnectionState: IceConnectionState): Unit = {
    if (iceConnectionState == IceConnectionState.DISCONNECTED || iceConnectionState == IceConnectionState.CLOSED) {
      redis.srem(registryPcsId(identifier), uuid)
    } else {
      redis.hset(registryPcId(identifier, uuid), "pub-ice-connection-state", iceConnectionState.name())
    }
  }

  override def onRegistryPubIceGatheringChange(identifier: String, uuid: String, iceGatheringState: IceGatheringState): Unit = {
    redis.hset(registryPcId(identifier, uuid), "pub-ice-connection-state", iceGatheringState.name())
  }

  override def onRegistryPubSignalingChange(identifier: String, uuid: String, signalState: SignalingState): Unit = {
    redis.hset(registryPcId(identifier, uuid), "pub-signal-state", signalState.name())
  }
}
