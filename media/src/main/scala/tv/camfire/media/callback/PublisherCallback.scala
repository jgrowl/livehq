package tv.camfire.media.callback

import org.webrtc.PeerConnection.{IceConnectionState, IceGatheringState, SignalingState}
import org.webrtc.{IceCandidate, MediaStream, SessionDescription}

/**
 * Created by jonathan on 12/15/13.
 * Updated by sarah on 11/26/14.
 */
trait PublisherCallback {

  def sendAnswer(identifier: String, answer: SessionDescription): Unit

  def sendIceCandidate(identifier: String, iceCandidate: IceCandidate): Unit

  def onIceConnectionChange(identifier: String, iceConnectionState: IceConnectionState): Unit

  def onIceGatheringChange(identifier: String, iceGatheringState: IceGatheringState): Unit

  def onSignalingChange(identifier: String, signalState: SignalingState): Unit

  def onAddStream(identifier: String, mediaStream: MediaStream): Unit

  def onRemoveStream(identifier: String, mediaStream: MediaStream): Unit

  def onRegistryPubInitialize(identifier: String, uuid: String, path: String)

  def onRegistryPubIceConnectionChange(identifier: String, uuid: String, iceConnectionState: IceConnectionState): Unit

  def onRegistryPubIceGatheringChange(identifier: String, uuid: String, iceGatheringState: IceGatheringState): Unit

  def onRegistryPubSignalingChange(identifier: String, uuid: String, signalState: SignalingState): Unit

}
