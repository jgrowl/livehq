package tv.camfire.actor

import akka.actor.{Actor, ActorLogging}
import livehq.{StandardPcDetails, Incoming, Internal, PcDetails}
import org.webrtc.PeerConnection.{IceConnectionState, IceGatheringState, SignalingState}
import org.webrtc.{DataChannel, IceCandidate, MediaStream, PeerConnection}
import tv.camfire.media.callback.Callback
import tv.camfire.media.webrtc.WebRtcHelper

import scala.collection.mutable

/**
 * User: jonathan
 * Date: 5/2/13
 * Time: 8:06 PM
 */
class Registry(webRtcHelper: WebRtcHelper, callback: Callback) extends Actor with ActorLogging {

  // Map identifiers to a PcDetails, not a uuid
  private val _pcDetails = mutable.Map.empty[String, PcDetails]

  override def receive: Receive = {
    case Internal.Offer(identifier, uuid, offer) =>
      log.info(s"($identifier).($uuid) offer received.")
      ensurePeerConnection(identifier, uuid)
      val answer = webRtcHelper.createAnswer(_pcDetails.get(identifier).get.peerConnection, offer)
      if (answer.isDefined) {
        sender ! Internal.Answer(identifier, uuid, answer.get)
      } else {
        log.error("Failed to create answer! No answer will be sent back for offer!")
      }
    case Internal.Candidate(identifier, uuid, candidate) =>
      log.info(s"($identifier).($uuid) candidate received.")
      ensurePeerConnection(identifier, uuid)
      _pcDetails.get(identifier).get.peerConnection.addIceCandidate(candidate)

      // TODO: Determine if we want to handle the local one differently. We could reduce the the number of streams by one
//    case AddMediaStream(identifier, mediaStream) =>
//      log.info(s"Registry().($identifier) MediaStream received: ${mediaStream.label()}")
//      pcLookup.get(uuid).get.addStream(mediaStream)
////      mediaStreams.put(identifier, mediaStream)

    case Incoming.Subscribe(identifier: String, targetIdentifier: String) =>
      log.info(s"($identifier) subscribing to $targetIdentifier.")
      _pcDetails.get(identifier).get.getMediaStreams.foreach {
        case (mediaStreamId, mediaStream) => {
          log.info(s"Attempting to add $mediaStreamId to $identifier...")
          callback.onSubscribe(identifier, targetIdentifier, mediaStream.label())
          sender ! Internal.AddMediaStream(identifier, mediaStreamId, mediaStream)
        }
      }

      // TODO: Add the ability to subscribe to a single label
  }

  def ensurePeerConnection(identifier: String, uuid: String): Unit = {
    if (!_pcDetails.contains(identifier)) {
      val observer = new PeerConnection.Observer() {
        override def onSignalingChange(signalState: SignalingState): Unit = {
          log.info(s"PeerConnection($identifier)($uuid).onSignalingChange : ${signalState.name()}")
          callback.onRegistryPubSignalingChange(identifier, uuid, signalState)
        }

        override def onError(): Unit = {
          log.error(s"PeerConnection($identifier)($uuid).onError!")
        }

        override def onIceCandidate(candidate: IceCandidate): Unit = {
          log.info(s"PeerConnection($identifier)($uuid).onIceCandidate : [$candidate].")
          sender ! Internal.Candidate(identifier, uuid, candidate)
        }

        override def onRemoveStream(mediaStream: MediaStream): Unit = {
          log.info(s"PeerConnection($identifier)($uuid).onRemoveStream.")
          callback.onRegistrySubRemoveStream(identifier, uuid, mediaStream)
        }

        override def onIceGatheringChange(gatheringState: IceGatheringState): Unit = {
          log.info(s"PeerConnection($identifier)($uuid).onIceGatheringChange : ${gatheringState.name()}")
          callback.onRegistrySubIceGatheringChange(identifier, uuid, gatheringState)
        }

        override def onIceConnectionChange(connectionState: IceConnectionState): Unit = {
          log.info(s"PeerConnection($identifier)($uuid).onIceConnectionChange : ${connectionState.name()}")
          callback.onRegistrySubIceConnectionChange(identifier, uuid, connectionState)
        }

        override def onAddStream(mediaStream: MediaStream): Unit = {
          log.info(s"PeerConnection($identifier)($uuid).onAddStream : ${mediaStream.label()}")
          callback.onRegistrySubAddStream(identifier, uuid, mediaStream)
          val duplicatedMediaStream = webRtcHelper.createDuplicateMediaStream(mediaStream, identifier)
          _pcDetails.get(identifier).get.addStream(duplicatedMediaStream)
        }

        override def onDataChannel(p1: DataChannel): Unit = ???
      }

      log.info(s"Creating PeerConnection($identifier)($uuid).")
      callback.onRegistrySubInitialize(identifier, uuid, self.path.toString)
      _pcDetails.put(identifier, new StandardPcDetails(webRtcHelper.createPeerConnection(observer)))
    }
  }
}
