import org.webrtc._


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
    case class Subscribe(identifier: String, targetIdentifier: String) extends Command with Incoming
  }

  object Internal {
    sealed trait Internal

    case class AddMediaStream(identifier: String, mediaStream: MediaStream) extends Command with Internal

    case class Offer(identifier: String, uuid: String, sessionDescription: SessionDescription) extends PeerConnectionCommand with Internal
    case class Answer(identifier: String, uuid: String, answer: SessionDescription) extends PeerConnectionCommand with Internal
    case class Candidate(identifier: String, uuid: String, iceCandidate: IceCandidate) extends PeerConnectionCommand with Internal

    case class CreateRegistryPeerConnections(identifier: String) extends Command with Internal
    case class AttachMediaStreams(identifier: String, uuid: String) extends PeerConnectionCommand with Internal
  }

  case class PcDetails(path: String, peerConnection: PeerConnection) {
    val mMediaStreams = scala.collection.mutable.HashMap.empty[String, MediaStream]

    def getStreamById(id: String): Option[MediaStream] = {
//      mMediaStreams.get(id)
      Some(mMediaStreams.last._2)
    }

    def addStream(mediaStream: MediaStream): Unit = {
      mMediaStreams.put(mediaStream.label(), mediaStream)
    }

    def attachStreams(pcDetails: PcDetails, mediaConstraints: MediaConstraints): Unit = {
      for (mediaStream <- mMediaStreams) {
        pcDetails.peerConnection.addStream(mediaStream._2, mediaConstraints)
      }
    }
  }

//  type PcLookup = scala.collection.mutable.HashMap[String, PcDetails]

//  object ErrorMessages {
//    val UNKNOWN_SIGNAL = "Unknown Signal"
//    val FAILED_SIGNAL_PARSE = "Could not parse a signal in the request"
//    val FAILED_DATA_PARSE = "Could not parse embedded data inside of the signal"
//    val CLIENT_ERROR = "There may be a problem with the javascript client or someone is trying to do something malicious"
//
//    // This user can really send anything they want. They could try to send a session that did not ever exist in the
//    // system. It could be indicative that there is a client side problem or that the user is up to something malicious.
//    val _NO_SESSION_COMPANION = "There was no associated companion for the requested session [%s] to forward the message [%s]!"
//
//    def NO_SESSION_COMPANION(sessionId: String, msg: AnyRef): String = {
//      _NO_SESSION_COMPANION.format(sessionId, msg)
//    }
//  }
//
//  sealed trait SessionCompanionEvent
//
//  sealed trait SessionManagerEvent
//
//  object Incoming {
//
//    sealed trait Incoming
//
//    case class RemoveSession(sessionId: String) extends SessionManagerEvent with Incoming
//
//    case class Offer(sessionId: String, remoteDescription: SessionDescription) extends SessionCompanionEvent with Incoming
//
//    case class Answer(sessionId: String, remoteDescription: SessionDescription) extends SessionCompanionEvent with Incoming
//
//    case class IceCandidate(sessionId: String, iceCandidate: org.webrtc.IceCandidate) extends SessionCompanionEvent with Incoming
//
//    case class Publish() extends SessionCompanionEvent with Incoming
//
//    case class Unpublish() extends SessionCompanionEvent with Incoming
//
//    case class Subscribe(sessionId: String, label: String) extends SessionCompanionEvent with Incoming
//
//    case class Unsubscribe(sessionId: String, resourceId: String) extends SessionCompanionEvent with Incoming
//
//  }
//
//  object Outgoing {
//
//    sealed trait Outgoing
//
//    case class Answer(description: SessionDescription) extends SessionCompanionEvent with Outgoing
//
//    case class Offer(description: SessionDescription) extends SessionCompanionEvent with Outgoing
//
//    case class IceCandidate(iceCandidate: org.webrtc.IceCandidate) extends SessionCompanionEvent with Outgoing
//
//  }

//  object Internal {
//
//    sealed trait Internal
//
//    case class AddSession(sessionId: String) extends SessionManagerEvent with Internal
//
//    case class AddStream(sessionId: String, mediaStream: MediaStream) extends SessionCompanionEvent with Internal
//
//    case class SubscribeNotification(requester: ActorRef, label: String) extends SessionCompanionEvent with Internal
//
//    case class Subscribe(mediaStream: MediaStream) extends SessionCompanionEvent with Internal
//
//  }


  //  case class GetAvailableStreams() extends SessionCompanionEvent
  //
  //  case class RegisterMediaStream(sessionId: String, mediaStream: MediaStream) extends SessionCompanionEvent
  //
  //  case class UnRegisterAllMediaStreams(sessionId: String) extends SessionCompanionEvent
  //
  //  case class TellActorMediaStream(actorRef: ActorRef) extends SessionCompanionEvent
  //
  //  case class ReceiveActorMediaStream(mediaStream: MediaStream) extends SessionCompanionEvent

}
