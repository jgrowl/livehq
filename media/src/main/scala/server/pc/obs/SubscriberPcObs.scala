package server.pc.obs

import akka.actor.{PoisonPill, ActorRef}
import akka.event.LoggingAdapter
import org.webrtc.PeerConnection.{IceConnectionState, IceGatheringState, SignalingState}
import org.webrtc.{DataChannel, IceCandidate, MediaStream, PeerConnection}
//import server.Subscriber.SendOffer
import server.Utils
import tv.camfire.media.callback.SubscriberCallback

/**
 * Created by jonathan on 12/4/14.
 */
class SubscriberPcObs(log: LoggingAdapter,
                      pcId: String,
                      self: ActorRef,
                      callback: SubscriberCallback,
                      identifier: String
                       ) extends PeerConnection.Observer {
  override def onRenegotiationNeeded() {
    log.warning(s"$pcId.onRenegotiationNeeded")
  }

  override def onIceCandidate(iceCandidate: IceCandidate): Unit = {
    log.info(s"$pcId.onIceCandidate. [${Utils.stripNewline(iceCandidate.toString)}]")
//    log.warning("Not actually sending ice candidate to disable trickling")
//    if (iceCandidate == null) {
//      log.warning("YO! ice candidate was null, we can send the sdp")
//      self ! SendOffer(identifier)
//
//    }
    callback.sendIceCandidate(identifier, iceCandidate)
  }

  override def onRemoveStream(mediaStream: MediaStream): Unit = {
    log.info(s"$pcId.onRemoveStream : [${mediaStream.label()}.")
    //    callback.onRemoveStream(identifier, mediaStream)
  }

  override def onIceGatheringChange(gatheringState: IceGatheringState): Unit = {
    log.info(s"$pcId.onIceGatheringChange : [${gatheringState.name()}].")
//    if (gatheringState == IceGatheringState.COMPLETE) {
//      // If trickling is turned on then we can send the sdp now
//      // todo: send sdp
//      self ! SendOffer(identifier)
//    }
  }

  override def onSignalingChange(signalState: SignalingState): Unit = {
    log.info(s"$pcId.onSignalingChange : [${signalState.name()}].")
    //    callback.onSignalingChange(identifier, signalState)
  }

  override def onIceConnectionChange(iceConnectionState: IceConnectionState): Unit = {
    log.info(s"$pcId.onIceConnectionChange : [${iceConnectionState.name()}].")
    callback.onIceConnectionChange(identifier, iceConnectionState)
    if (iceConnectionState == IceConnectionState.CONNECTED) {
    } else if (iceConnectionState == IceConnectionState.DISCONNECTED) {
      self.tell(PoisonPill.getInstance, self)
    }
  }

  override def onAddStream(mediaStream: MediaStream): Unit = {
    log.info(s"$pcId.onAddStream : [${mediaStream.label()}]")
    //    callback.onAddStream(identifier, mediaStream)
    //    self.tell(Internal.Publisher.AddStream(identifier, mediaStream), self)
  }

  override def onDataChannel(p1: DataChannel): Unit = ???
}
