import org.webrtc._
import tv.camfire.media.webrtc.WebRtcHelper

import scala.collection.mutable

/**
 * User: jonathan
 * Date: 7/22/13
 * Time: 11:26 PM
 */
package object livehq {

  sealed trait Command {
    def identifier: String
  }

  sealed trait PeerConnectionCommand extends Command {
    def uuid: String
  }

  object Incoming {
    sealed trait Incoming

    case class Offer(identifier: String, sessionDescription: SessionDescription) extends Command with Incoming
    case class Candidate(identifier: String, iceCandidate: IceCandidate) extends Command with Incoming
    case class Subscribe(identifier: String, publisherIdentifier: String) extends Command with Incoming
  }

  object Internal {
    sealed trait Internal

    object Publisher {
      sealed trait Publisher
      case class AddStream(identifier: String, mediaStream: MediaStream) extends Command with Internal with Publisher

    }

    object Registry {
      sealed trait Registry
      case class Connected(identifier: String) extends Command with Internal
      case class Initialize(identifier: String) extends Command with Internal
      case class AddStream(identifier: String, mediaStream: MediaStream) extends Command with Internal
    }

//    case class AddRegistryMediaStream(identifier: String, mediaStreamId: String, mediaStream: MediaStream) extends Command with Internal
    case class AddRegistryMediaStream(mediaStreamId: String, mediaStream: MediaStream) extends Internal

    case class Offer(identifier: String, uuid: String, sessionDescription: SessionDescription) extends PeerConnectionCommand with Internal
    case class Answer(identifier: String, uuid: String, answer: SessionDescription) extends PeerConnectionCommand with Internal
    case class Candidate(identifier: String, uuid: String, iceCandidate: IceCandidate) extends PeerConnectionCommand with Internal

    case class CreateRegistryPeerConnections(identifier: String) extends Command with Internal
//    case class AddStream(identifier: String, mediaStream: MediaStream) extends Command with Internal
    case class CleanRegistryPeerConnections(identifier: String) extends Command with Internal

    case class AttachMediaStreams(identifier: String, uuid: String) extends PeerConnectionCommand with Internal

    case class RequestPeerConnection(identifier: String, uuid: String) extends Command with Internal
  }

  class PcDetails(val peerConnection: PeerConnection, val webRtcHelper: WebRtcHelper) {
//    case class PcDetails(path: String, peerConnection: PeerConnection) {
    val mMediaStreams = mutable.Map.empty[String, MediaStream]
    val mDuplicatedMediaStreams = mutable.Map.empty[String, MediaStream]

    def getStreamById(id: String): Option[MediaStream] = {
      // TODO: Hard coding for testing purposes
//      mMediaStreams.get(id)
      Some(mMediaStreams.last._2)
    }

    def addStream(mediaStream: MediaStream): Unit = {
      val label = mediaStream.label()
      mMediaStreams.put(label, mediaStream)
      val duplicatedLabel = s"$label-dup"
      val duplicatedMediaStream = webRtcHelper.createDuplicateMediaStream(mediaStream, duplicatedLabel)
      mDuplicatedMediaStreams.put(duplicatedLabel, duplicatedMediaStream)
    }

    def getMediaStreams: mutable.Map[String, MediaStream] = {
      mMediaStreams
    }

    def getDuplicatedMediaStreams: mutable.Map[String, MediaStream] = {
      mDuplicatedMediaStreams
    }
  }

  case class StandardPcDetails(override val peerConnection: PeerConnection,
                               override val webRtcHelper: WebRtcHelper) extends PcDetails(peerConnection, webRtcHelper) {}

  case class PathedPcDetails(path: String,
                             override val peerConnection: PeerConnection,
                             override val webRtcHelper: WebRtcHelper) extends PcDetails(peerConnection, webRtcHelper) {}

  object Log {
    def pcId(identifier: String): String = {
      s"Pc($identifier)"
    }

    private def _registryPcId(identifier: String, uuid: String): String = {
      s"${pcId(identifier)}($uuid)"
    }

    def registryPubPcId(identifier: String, uuid: String): String = {
      s"${_registryPcId(identifier, uuid)}(pub)"
    }

    def registrySubPcId(identifier: String, uuid: String): String = {
      s"${_registryPcId(identifier, uuid)}(sub)"
    }
  }
}
