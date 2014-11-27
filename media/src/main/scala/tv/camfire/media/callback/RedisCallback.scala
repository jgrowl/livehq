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
class RedisCallback(properties: Properties, redis: RedisClient)() extends Callback with jackson.JsonMethods
with WebrtcSerializationSupport {
  val log = LoggerFactory.getLogger(getClass)
  val apiBase = "%s/api/v1".format(properties.restUrl)

  def publisherInfoId(identifier: String): String = {
    s"publisher:$identifier"
  }

  def publisherRegistryInfoId(identifier: String, uuid: String): String = {
    s"publisher:$identifier:registry:$uuid"
  }

  def mediaStreamId(identifier: String): String = {
    s"publisher:$identifier:streams"
  }

  def registryStreamId(identifier: String, uuid: String): String = {
    s"registry:$identifier:$uuid:streams"
  }

  override def sendAnswer(identifier: String, answer: SessionDescription): Unit = {
    redis.publish("web.webrtc.answer:%s".format(identifier), mapper.writeValueAsString(answer))
  }

  override def sendOffer(identifier: String, answer: SessionDescription): Unit = {
    redis.publish("web.webrtc.offer:%s".format(identifier), mapper.writeValueAsString(answer))
  }

  override def sendIceCandidate(identifier: String, iceCandidate: IceCandidate): Unit = {
    redis.publish("web.webrtc.ice-candidate:%s".format(identifier), mapper.writeValueAsString(iceCandidate))
  }

  override def onAddStream(identifier: String, mediaStream: MediaStream): Unit = {
    redis.sadd(mediaStreamId(identifier), mediaStream.label())
  }

  override def onRemoveStream(identifier: String, mediaStream: MediaStream): Unit = {
    redis.srem(mediaStreamId(identifier), mediaStream.label())
  }

  override def onIceConnectionChange(identifier: String, iceConnectionState: IceConnectionState): Unit = {
    redis.hset(s"${publisherInfoId(identifier)}", "ice-connection-state", iceConnectionState.name())
  }

  override def onIceGatheringChange(identifier: String, iceGatheringState: IceGatheringState): Unit = {
    redis.hset(s"${publisherInfoId(identifier)}", "ice-connection-state", iceGatheringState.name())
  }

  override def onSignalingChange(identifier: String, signalState: SignalingState): Unit = {
    redis.hset(s"${publisherInfoId(identifier)}", "signal-state", signalState.name())
  }

  ////////////////

  override def onRegistryPubInitialize(identifier: String, uuid: String, path: String): Unit = {
    redis.hset(s"${publisherRegistryInfoId(identifier, uuid)}", "path", path)
  }

  override def onRegistryPubIceConnectionChange(identifier: String, uuid: String, iceConnectionState: IceConnectionState): Unit = {
    redis.hset(s"${publisherRegistryInfoId(identifier, uuid)}", "pub-ice-connection-state", iceConnectionState.name())
  }

  override def onRegistryPubIceGatheringChange(identifier: String, uuid: String, iceGatheringState: IceGatheringState): Unit = {
    redis.hset(s"${publisherRegistryInfoId(identifier, uuid)}", "pub-ice-connection-state", iceGatheringState.name())
  }

  override def onRegistryPubSignalingChange(identifier: String, uuid: String, signalState: SignalingState): Unit = {
    redis.hset(s"${publisherRegistryInfoId(identifier, uuid)}", "pub-signal-state", signalState.name())
  }

  override def onRegistryPubAddStream(identifier: String, uuid: String, mediaStream: MediaStream): Unit = {
    redis.sadd(s"${registryStreamId(identifier, uuid)}", mediaStream.label())
  }

  override def onRegistryPubRemoveStream(identifier: String, uuid: String, mediaStream: MediaStream): Unit = {
    redis.srem(s"${registryStreamId(identifier, uuid)}", mediaStream.label())
  }

  override def onRegistrySubInitialize(identifier: String, uuid: String, path: String): Unit = {
    redis.hset(s"${publisherRegistryInfoId(identifier, uuid)}", "sub-path", path)
  }

  override def onRegistrySubIceConnectionChange(identifier: String, uuid: String, iceConnectionState: IceConnectionState): Unit = {
    redis.hset(s"${publisherRegistryInfoId(identifier, uuid)}", "sub-ice-connection-state", iceConnectionState.name())
  }

  override def onRegistrySubIceGatheringChange(identifier: String, uuid: String, iceGatheringState: IceGatheringState): Unit = {
    redis.hset(s"${publisherRegistryInfoId(identifier, uuid)}", "sub-ice-connection-state", iceGatheringState.name())
  }

  override def onRegistrySubAddStream(identifier: String, uuid: String, mediaStream: MediaStream): Unit = {
    redis.sadd(s"${registryStreamId(identifier, uuid)}", mediaStream.label())
  }

  override def onRegistrySubRemoveStream(identifier: String, uuid: String, mediaStream: MediaStream): Unit = {
    redis.srem(s"${registryStreamId(identifier, uuid)}", mediaStream.label())
  }

  override def onRegistrySubSignalingChange(identifier: String, uuid: String, signalState: SignalingState): Unit = {
    redis.hset(s"${publisherRegistryInfoId(identifier, uuid)}", "sub-ice-signal-state", signalState.name())
  }
}
