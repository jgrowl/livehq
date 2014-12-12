package server.pc.obs

import akka.actor.{ActorRef, ActorSystem}
import akka.event.LoggingAdapter
import livehq.Internal
import org.webrtc.PeerConnection.{IceConnectionState, IceGatheringState, SignalingState}
import org.webrtc.{DataChannel, IceCandidate, MediaStream, PeerConnection}
import server.Utils
import tv.camfire.media.callback.Callback

/**
 * Created by jonathan on 12/4/14.
 */
class PublisherToRegistryPcObs(log: LoggingAdapter,
                                     logId: String,
                                     system: ActorSystem,
                                     path: String,
                                     self: ActorRef,
                                     callback: Callback,
                                     identifier: String,
                                     uuid: String) extends PeerConnection.Observer {

    val registry = system.actorSelection(path)

    override def onSignalingChange(signalState: SignalingState): Unit = {
      log.info(s"$logId.onSignalingChange : [${signalState.name()}].")
      callback.onRegistryPubSignalingChange(identifier, uuid, signalState)
    }

    override def onError(): Unit = {
      log.error(s"$logId.onError!")
    }

    override def onIceCandidate(candidate: IceCandidate): Unit = {
      log.info(s"$logId.onIceCandidate [${Utils.stripNewline(candidate.toString)}]")
      registry.tell(Internal.Candidate(identifier, uuid, candidate), self)
    }

    override def onIceGatheringChange(gatheringState: IceGatheringState): Unit = {
      log.info(s"$logId.onIceGatheringChange : [${gatheringState.name()}].")
      callback.onRegistryPubIceGatheringChange(identifier, uuid, gatheringState)
    }

    override def onIceConnectionChange(iceConnectionState: IceConnectionState): Unit = {
      log.info(s"$logId.onIceConnectionChange : [${iceConnectionState.name()}].")
      callback.onRegistryPubIceConnectionChange(identifier, uuid, iceConnectionState)
      if (iceConnectionState == IceConnectionState.CONNECTED) {
//        // Now that we're connected to the registry PeerConnection, we can add any MediaStreams
//        log.info(s"$logId Attaching MediaStreams.")
//
//        self.tell(Internal.AttachMediaStreams(identifier, uuid), self)
      } else if (iceConnectionState == IceConnectionState.CLOSED) {
      }
    }

    // Registry PeerConnections(pub) are only outgoing! It would never make sense to have a Registry PeerConnection
    // ever send anything back to the Publisher since the registry only acts as a means of replication!
    override def onAddStream(mediaStream: MediaStream): Unit = {
      log.error(s"$logId.onAddStream : [${mediaStream.label()}] called but the registry should never do this!")
    }

    // onAddStream should never get called, thus onRemove will never get called either!
    override def onRemoveStream(mediaStream: MediaStream): Unit = {
      log.error(s"$logId.onRemoveStream : [${mediaStream.label()}.")
    }

    override def onDataChannel(p1: DataChannel): Unit = ???
}
