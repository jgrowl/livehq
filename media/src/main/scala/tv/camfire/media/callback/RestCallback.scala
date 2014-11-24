package tv.camfire.media.callback

import org.webrtc.{IceCandidate, SessionDescription}
import tv.camfire.media.config.Properties
import akka.actor.ActorSystem
import org.slf4j.LoggerFactory
//import dispatch._, Defaults._


/**
 * Created by jonathan on 12/15/13.
 */
class RestCallback(properties: Properties)() extends Callback {

  val log = LoggerFactory.getLogger(getClass)
  val apiBase = "%s/api/v1".format(properties.restUrl)


  override def sendAnswer(identifier: String, answer: SessionDescription): Unit = {

  }

  def onAddSession(sessionId: String): Unit = {
//    log.info("Adding session {}", sessionId)
//    val json = """{"session": {"sid": "%s"}}""".format(sessionId)
//    val svc = url("%s/session".format(apiBase)).POST.addHeader("Content-type", "application/json") << json
//    Http(svc OK as.String)
  }

  def onRemoveSession(sessionId: String): Unit = {
//    log.info("Removing session {}", sessionId)
//    val json = """{"session": {"sid": "%s"}}""".format(sessionId)
//    val svc = url("%s/session".format(apiBase)).DELETE.addHeader("Content-type", "application/json") << json
//    Http(svc OK as.String)
  }

  def onAddStream(sessionId: String, streamId: String): Unit = {
//    log.info("Adding stream {} to session {}{}", streamId, sessionId, "")
//    val json = """{"stream": {"sid": "%s", "label": "%s"}}""".format(sessionId, streamId)
//    val svc = url("%s/stream".format(apiBase)).POST.addHeader("Content-type", "application/json") << json
//    Http(svc OK as.String)
  }

  def onRemoveStream(sessionId: String, streamId: String): Unit = {
//    log.info("Removing stream {} from session {}{}", streamId, sessionId, "")
//    val svc = url("%s/stream/sid/%s".format(apiBase, sessionId)).DELETE.addHeader("Content-type", "application/json")
//    Http(svc OK as.String)
  }

  def removeStreamsFromSession(sessionId: String): Unit = {
//    log.info("Send notification to remove all streams from session {}", sessionId)
//    val svc = url("%s/session/%s/streams".format(apiBase, sessionId)).DELETE.addHeader("Content-type", "application/json")
//    Http(svc OK as.String)
  }

  def onPublish(): Unit = {}

  def onUnpublish(): Unit = {}

  override def sendIceCandidate(identifier: String, iceCandidate: IceCandidate): Unit = ???

  override def sendOffer(identifier: String, answer: SessionDescription): Unit = ???
}
