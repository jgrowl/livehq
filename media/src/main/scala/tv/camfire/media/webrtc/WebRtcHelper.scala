package tv.camfire.media.webrtc

import org.webrtc._
import java.util
import org.slf4j.{LoggerFactory, Logger}
import org.webrtc.MediaConstraints.KeyValuePair
import tv.camfire.media.Types._
import tv.camfire.media.config.factory.PeerConnectionObserverFactoryFactory
import akka.actor.{ActorRef, ActorSystem}
import tv.camfire.media.callback.Callback

/**
 * User: jonathan
 * Date: 4/24/13
 * Time: 1:45 PM
 */
class WebRtcHelper(peerConnectionObserverFactoryFactory: PeerConnectionObserverFactoryFactory,
                   val factory: PeerConnectionFactory,
                   iceServers: util.ArrayList[PeerConnection.IceServer]) {
  private val peerConnectionObserverFactory: PeerConnectionObserverFactory = peerConnectionObserverFactoryFactory.create()

  val log: Logger = LoggerFactory.getLogger(classOf[WebRtcHelper])
    def createPeerConnection(identifier: String): PeerConnection = {
      factory.createPeerConnection(iceServers, createConstraints, peerConnectionObserverFactory.create(identifier))
    }

//  def createPeerConnection(sessionId: String, callback: Callback, sender: ActorRef)
//                          (implicit actorSystem: ActorSystem, resourceUuid: String): PeerConnection = {
//    val observer = peerConnectionObserverFactory.create(sessionId, callback, sender)
//    factory.createPeerConnection(iceServers, createConstraints, observer)
//  }
//
//  def createPeerConnection(sessionId: String, observer: PeerConnection.Observer) = {
//    factory.createPeerConnection(iceServers, createConstraints, observer)
//  }
//
//  def createPeerConnection(constraints: MediaConstraints, observer: PeerConnectionObserver): PeerConnection = {
//    factory.createPeerConnection(iceServers, createConstraints, observer)
//  }

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

  def establishPeerConnection(peerConnection: PeerConnection, remoteSessionDescription: SessionDescription) {
    setRemoteDescription(peerConnection, remoteSessionDescription)

    val sdpLatch = new SdpObserverLatch()
    peerConnection.createAnswer(sdpLatch, createConstraints)
    if (sdpLatch.await()) {
      log.info("Successfully created answer.")
      val answer = sdpLatch.sdp
      val sdpLatch2 = new SdpObserverLatch
      peerConnection.setLocalDescription(sdpLatch2, answer)
      if (sdpLatch2.await()) {
        log.info("Successfully set local description.")
      } else {
        log.error("There was a problem setting the local description!")
      }
    } else {
      log.error(sdpLatch.error)
    }
  }

  def makeOffer(peerConnection: PeerConnection): PeerConnection = {
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
      } else {
        log.error("Failed to set local description!")
      }
    } else {
      log.error("Failed to create offer!")
    }

    peerConnection
  }

  def createConstraints: MediaConstraints = {
    val constraints = new MediaConstraints()
    constraints.mandatory.add(new KeyValuePair("OfferToReceiveVideo", "true"))
    constraints.mandatory.add(new KeyValuePair("OfferToReceiveAudio", "true"))


//    constraints.mandatory.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))

//    constraints.optional.add(
//      new MediaConstraints.KeyValuePair("RtpDataChannels", "true"))



    constraints
  }

}



