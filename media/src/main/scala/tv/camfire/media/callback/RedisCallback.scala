package tv.camfire.media.callback

import org.json4s.jackson
import org.slf4j.LoggerFactory
import org.webrtc.{IceCandidate, SessionDescription}
import redis.RedisClient
import tv.camfire.media.config.Properties
import tv.camfire.webrtc.serialization.jackson.WebrtcSerializationSupport

//import dispatch._, Defaults._


/**
 * Created by jonathan on 12/15/13.
 */
class RedisCallback(properties: Properties, redis: RedisClient)() extends Callback with jackson.JsonMethods
with WebrtcSerializationSupport {
  val log = LoggerFactory.getLogger(getClass)
  val apiBase = "%s/api/v1".format(properties.restUrl)


  override def sendAnswer(identifier: String, answer: SessionDescription): Unit = {
    redis.publish("web.webrtc.answer:%s".format(identifier), mapper.writeValueAsString(answer))
  }

  override def sendIceCandidate(identifier: String, iceCandidate: IceCandidate): Unit = {
    redis.publish("web.webrtc.ice-candidate:%s".format(identifier), mapper.writeValueAsString(iceCandidate))
  }

  def onAddSession(sessionId: String): Unit = {

  }

  def onRemoveSession(sessionId: String): Unit = {
  }

  def onAddStream(sessionId: String, streamId: String): Unit = {
  }

  def onRemoveStream(sessionId: String, streamId: String): Unit = {
  }

  def removeStreamsFromSession(sessionId: String): Unit = {
  }

  def onPublish(): Unit = {}

  def onUnpublish(): Unit = {}

}
