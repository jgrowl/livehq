package tv.camfire.media.callback

import org.webrtc.PeerConnection.{IceGatheringState, SignalingState, IceConnectionState}
import org.webrtc.{MediaStream, IceCandidate, SessionDescription}

/**
 * Created by jonathan on 12/15/13.
 * Updated by sarah on 11/26/14.
 */
trait SubscriberCallback {

    def sendIceCandidate(identifier: String, iceCandidate: IceCandidate): Unit

    def onIceConnectionChange(identifier: String, iceConnectionState: IceConnectionState): Unit

    def sendOffer(identifier: String, answer: SessionDescription): Unit

    def onSubscribe(identifier: String, target: String, label: String): Unit

    def onRegistryPubSignalingChange(identifier: String, uuid: String, signalState: SignalingState): Unit

    def onRegistrySubInitialize(identifier: String, uuid: String, path: String)

    def onRegistrySubIceConnectionChange(identifier: String, uuid: String, iceConnectionState: IceConnectionState): Unit

    def onRegistrySubIceGatheringChange(identifier: String, uuid: String, iceGatheringState: IceGatheringState): Unit

    def onRegistrySubSignalingChange(identifier: String, uuid: String, signalState: SignalingState): Unit

    def onRegistrySubAddStream(identifier: String, uuid: String, mediaStream: MediaStream): Unit

    def onRegistrySubRemoveStream(identifier: String, uuid: String, mediaStream: MediaStream): Unit

}
