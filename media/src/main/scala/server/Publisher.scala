package server

import akka.actor.{ActorLogging, PoisonPill, Props, ReceiveTimeout}
import akka.contrib.pattern.ShardRegion
import akka.contrib.pattern.ShardRegion.Passivate
import akka.persistence.PersistentActor
import org.webrtc.{IceCandidate, PeerConnection, SessionDescription}
import server.Publisher.{PeerConnectionAdded, State}
import tv.camfire.media.callback.Callback
import tv.camfire.media.webrtc.WebRtcHelper
import Publisher.Incoming

object Publisher {

  def props(webrtcHelper: WebRtcHelper, callback: Callback): Props =
    Props(new Publisher(webrtcHelper, callback))

  sealed trait Command {
    def identifier: String
  }

  object Incoming {
    sealed trait Incoming

    case class AddPeerConnection(identifier: String) extends Command
    case class Offer(identifier: String, sessionDescription: SessionDescription) extends Command with Incoming
    case class Candidate(identifier: String, iceCandidate: IceCandidate) extends Command with Incoming
  }

  sealed trait Event
  case class PeerConnectionAdded(pc: PeerConnection) extends Event

  val idExtractor: ShardRegion.IdExtractor = {
    case cmd: Command => (cmd.identifier, cmd)
  }

  val shardResolver: ShardRegion.ShardResolver = msg => msg match {
    case cmd: Command => (math.abs(cmd.identifier.hashCode) % 100).toString
  }

  val shardName: String = "Publisher"

  private case class State(peerConnection: PeerConnection) {

    def updated(evt: Event): State = evt match {
      case PeerConnectionAdded(pc)   => copy(peerConnection = pc)
    }
  }
}

class Publisher(webRtcHelper: WebRtcHelper, callback: Callback) extends PersistentActor with ActorLogging {

  // self.path.parent.name is the type name (utf-8 URL-encoded)
  // self.path.name is the entry identifier (utf-8 URL-encoded)
  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  // passivate the entity when no activity
//  context.setReceiveTimeout(2.minutes)

  private var state = State(null)

  override def receiveRecover: Receive = {
//        case evt: PeerConnectionAdded =>
//          context.become(created)
//          state = state.updated(evt)
//    case evt: PostAdded =>
//      context.become(created)
//      state = state.updated(evt)
//    case evt @ PostPublished =>
//      context.become(created)
//      state = state.updated(evt)
//    case evt: Event => state =
//      state.updated(evt)
    case _ =>
      log.error("Should not hit this yet")

  }

  override def receiveCommand: Receive = initial

  def initial: Receive = {
    case Incoming.AddPeerConnection(identifier: String) =>
      log.info("Creating PeerConnection for [{}]", identifier)
      val pc = webRtcHelper.createPeerConnection(identifier)
      state = state.updated(PeerConnectionAdded(pc))
      context.become(created)

//      persist(PeerConnectionAdded(pc)) { evt =>
//        state = state.updated(evt)
//        context.become(created)
//        log.info("New PeerConnection created: {}", "id for pc")
//      }
    case _ =>
      println("should not be here")
  }

  def created: Receive = {
    case Incoming.Offer(identifier, sessionDescription) =>
      log.debug("Establishing peerConnection for [{}]...", identifier)
      webRtcHelper.establishPeerConnection(state.peerConnection, sessionDescription)
      callback.sendAnswer(identifier, state.peerConnection.getLocalDescription)
    case Incoming.Candidate(identifier, iceCandidate) =>
      log.debug("Adding iceCandidate to [{}]...", identifier)
      state.peerConnection.addIceCandidate(iceCandidate)
    case _ =>
      print("herptidyderpty");
  }

  override def unhandled(msg: Any): Unit = msg match {
    case ReceiveTimeout => context.parent ! Passivate(stopMessage = PoisonPill)
    case _              => super.unhandled(msg)
  }

}
