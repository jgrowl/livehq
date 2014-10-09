package tv.camfire.media.webrtc

import org.webrtc._
import org.webrtc.PeerConnection.{IceConnectionState, IceGatheringState, SignalingState}
import org.slf4j.LoggerFactory
import akka.actor.{ActorSystem, ActorRef}
import tv.camfire.common.{Internal, Outgoing}
import tv.camfire.media.callback.Callback

/**
 * User: jonathan
 * Date: 4/28/13
 * Time: 2:10 PM
 */
class PeerConnectionObserver(identifier: String, callback: Callback) extends PeerConnection.Observer {

  val log = LoggerFactory.getLogger(classOf[PeerConnectionObserver])

  def onSignalingChange(p1: SignalingState) {
    log.debug("onSignalingChange called: [%s]...".format(p1.name()))
  }

  def onIceConnectionChange(p1: IceConnectionState) {
    log.debug("onIceConnectionChange called: [%s]...".format(p1.name()))
    if (IceConnectionState.DISCONNECTED.equals(p1)) {
//      log.debug("Connection [%s] disconnected, un-registering media streams...".format(sessionId))
//      callback.removeStreamsFromSession(sessionId)
    }
  }

  def onIceGatheringChange(p1: IceGatheringState) {
    log.debug("onIceGatheringChange called: [%s]...".format(p1.name()))
  }

  def onIceCandidate(iceCandidate: IceCandidate) {
    log.debug("onIceCandidate called...")
    callback.sendIceCandidate(identifier, iceCandidate)
  }

  def onError() {
    log.error("onError called...")
  }

  def onAddStream(mediaStream: MediaStream) {
    log.debug("onAddStream called...")
//    mediaService ! Internal.AddStream(sessionId, mediaStream)
//    callback.onAddStream(sessionId, mediaStream.label())
  }

  def onRemoveStream(mediaStream: MediaStream) {
    log.debug("onRemoveStream called...")
//    callback.onRemoveStream(sessionId, mediaStream.label())
  }

  def onDataChannel(p1: DataChannel): Unit = ???
}
