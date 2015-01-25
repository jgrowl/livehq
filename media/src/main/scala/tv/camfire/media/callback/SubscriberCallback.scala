package tv.camfire.media.callback

import org.webrtc.{IceCandidate, SessionDescription}

/**
 * Created by jonathan on 12/15/13.
 * Updated by sarah on 11/26/14.
 */
trait SubscriberCallback {
  def sendAnswer(identifier: String, answer: SessionDescription): Unit

  def sendOffer(identifier: String, answer: SessionDescription): Unit

  def sendIceCandidate(identifier: String, iceCandidate: IceCandidate): Unit
}
