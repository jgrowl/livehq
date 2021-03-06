package server.pc.obs

import akka.actor.{ActorRef, PoisonPill}
import akka.event.LoggingAdapter
import livehq.Internal
import org.webrtc.PeerConnection.{IceConnectionState, IceGatheringState, SignalingState}
import org.webrtc.{DataChannel, IceCandidate, MediaStream, PeerConnection}
import server.Utils
import tv.camfire.media.callback.SubscriberCallback
import tv.camfire.media.webrtc.WebRtcHelper

/**
 * Created by jonathan on 12/12/14.
 */
class RegistryToPublisherPcObs(
                        log: LoggingAdapter,
                        logId: String,
                        webRtcHelper: WebRtcHelper,
                        self: ActorRef,
                        callback: SubscriberCallback,
                        identifier: String,
                        uuid: String
                        ) extends PeerConnection.Observer {


  override def onSignalingChange(signalState: SignalingState): Unit = {
    log.info(s"$logId.onSignalingChange : ${signalState.name()}")
    callback.onRegistryPubSignalingChange(identifier, uuid, signalState)
  }

  override def onRenegotiationNeeded() {

  }

  override def onIceCandidate(candidate: IceCandidate): Unit = {
    log.info(s"$logId.onIceCandidate [${Utils.stripNewline(candidate.toString)}]")
//    sender.tell(Internal.Candidate(identifier, uuid, candidate), self)
    self.tell(Internal.Candidate(identifier, uuid, candidate), self)
  }

  override def onRemoveStream(mediaStream: MediaStream): Unit = {
    log.info(s"$logId.onRemoveStream.")
    callback.onRegistrySubRemoveStream(identifier, uuid, mediaStream)
  }

  override def onIceGatheringChange(gatheringState: IceGatheringState): Unit = {
    log.info(s"$logId.onIceGatheringChange : ${gatheringState.name()}")
    callback.onRegistrySubIceGatheringChange(identifier, uuid, gatheringState)
  }

  override def onIceConnectionChange(iceConnectionState: IceConnectionState): Unit = {
    log.info(s"$logId.onIceConnectionChange : ${iceConnectionState.name()}")
    callback.onRegistrySubIceConnectionChange(identifier, uuid, iceConnectionState)

    if (iceConnectionState == IceConnectionState.CONNECTED) {
      self.tell(Internal.Registry.Connected(identifier), self)
    } else if(iceConnectionState == IceConnectionState.DISCONNECTED) {
      self.tell(PoisonPill.getInstance, self)
    }
  }

  override def onAddStream(mediaStream: MediaStream): Unit = {
    log.info(s"$logId.onAddStream : ${mediaStream.label()}")
    callback.onRegistrySubAddStream(identifier, uuid, mediaStream)
    self.tell(Internal.Registry.AddStream(identifier, mediaStream), self)
  }

  override def onDataChannel(p1: DataChannel): Unit = ???
}
