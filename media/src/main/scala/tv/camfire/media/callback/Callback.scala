package tv.camfire.media.callback

import org.webrtc.{IceCandidate, SessionDescription}

/**
 * Created by jonathan on 12/15/13.
 */
trait Callback {

  def sendAnswer(identifier: String, answer: SessionDescription): Unit

  def sendOffer(identifier: String, answer: SessionDescription): Unit

  def sendIceCandidate(identifier: String, iceCandidate: IceCandidate): Unit

  def onAddSession(sessionId: String): Unit

  def onRemoveSession(sessionId: String): Unit

  def onPublish(): Unit

  def onUnpublish(): Unit

  def onAddStream(sessionId: String, streamId: String): Unit

  def onRemoveStream(sessionId: String, streamId: String): Unit

  def removeStreamsFromSession(sessionId: String): Unit
}
