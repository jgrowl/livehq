package tv.camfire.media.webrtc

import java.util

import org.slf4j.{Logger, LoggerFactory}
import org.webrtc.MediaConstraints.KeyValuePair
import org.webrtc._
import tv.camfire.media.factory.CamfirePeerConnectionFactory

/**
 * User: jonathan
 * Date: 4/24/13
 * Time: 1:45 PM
 */
class WebRtcHelper(val factory: CamfirePeerConnectionFactory,
                   iceServers: util.ArrayList[PeerConnection.IceServer]) {

  val log: Logger = LoggerFactory.getLogger(classOf[WebRtcHelper])

  def createDuplicateMediaStream(mediaStream: MediaStream, identifier: String): MediaStream = {
    factory.createDuplicatedMediaStream(mediaStream, identifier)
  }

    def createPeerConnection(observer: PeerConnection.Observer): PeerConnection = {
      factory.createPeerConnection(iceServers, createConstraints, observer)
    }

  def setRemoteDescription(peerConnection: PeerConnection, remoteSessionDescription: SessionDescription) {
    val sdpLatch = new SdpObserverLatch
    log.debug("Attempting to set remote description...")
    peerConnection.setRemoteDescription(sdpLatch, remoteSessionDescription)
    if (sdpLatch.await()) {
      log.info("Successfully set remote description.")
    } else {
      log.error(sdpLatch.error)
    }
  }

  def createAnswer(peerConnection: PeerConnection, offer: SessionDescription) : Option[SessionDescription] = {
    setRemoteDescription(peerConnection, offer)

    val sdpLatch = new SdpObserverLatch()
    peerConnection.createAnswer(sdpLatch, createConstraints)
    if (sdpLatch.await()) {
      log.info("Successfully created answer.")
      val answer = sdpLatch.sdp
      val sdpLatch2 = new SdpObserverLatch
      peerConnection.setLocalDescription(sdpLatch2, answer)
      if (sdpLatch2.await()) {
        log.info("Successfully set local description.")
        return Some(peerConnection.getLocalDescription)
      } else {
        log.error("There was a problem setting the local description!")
      }
    } else {
      log.error(sdpLatch.error)
    }

    None
  }

  def createOffer(peerConnection: PeerConnection): Option[SessionDescription] = {
    log.debug("Attempting to create offer...")
    val sdpLatch = new SdpObserverLatch
    peerConnection.createOffer(sdpLatch, createConstraints)
    if (sdpLatch.await()) {
      log.debug("Successfully created offer.")
      val localSessionDescription = sdpLatch.sdp
      log.debug("Attempting to set local description...")
      peerConnection.setLocalDescription(sdpLatch, localSessionDescription)
      if (sdpLatch.await()) {
        log.debug("Successfully set local description.")
        return Some(peerConnection.getLocalDescription)
      } else {
        log.error(s"Failed to set local description!: ${sdpLatch.error}")
      }
    } else {
      log.error(s"Failed to create offer!: ${sdpLatch.error}")
    }

    None
  }

  def createConstraints: MediaConstraints = {
    val constraints = new MediaConstraints()
//    constraints.mandatory.add(new KeyValuePair("OfferToReceiveVideo", "true"))
//    constraints.mandatory.add(new KeyValuePair("OfferToReceiveAudio", "true"))

    constraints.optional.add(new KeyValuePair("RtpDataChannels", "true"))
    constraints.optional.add(new KeyValuePair("DtlsSrtpKeyAgreement", "false"))

    constraints
  }

}



