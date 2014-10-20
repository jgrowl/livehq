package server

import akka.actor._
import akka.cluster.MemberStatus
import akka.contrib.pattern.ShardRegion
import akka.contrib.pattern.ShardRegion.Passivate
import akka.persistence.PersistentActor
import org.webrtc.PeerConnection.{IceConnectionState, IceGatheringState, SignalingState}
import org.webrtc._
import server.Publisher._
import tv.camfire.actor.Registry
import tv.camfire.actor.Registry.Internal
import tv.camfire.media.callback.Callback
import tv.camfire.media.webrtc.WebRtcHelper

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
    case class Subscribe(identifier: String, targetIdentifier: String) extends Command with Incoming
  }

  sealed trait Event
  case class PeerConnectionsAdded(pcs: (PeerConnection, scala.collection.mutable.HashMap[String, PeerConnection])) extends Event
  case class MediaStreamAdded(ms: MediaStream) extends Event

  val idExtractor: ShardRegion.IdExtractor = {
    case cmd: Command => (cmd.identifier, cmd)
  }

  val shardResolver: ShardRegion.ShardResolver = msg => msg match {
    case cmd: Command => (math.abs(cmd.identifier.hashCode) % 100).toString
  }

  val shardName: String = "Publisher"

  private case class State(peerConnection: PeerConnection, clusterPeerConnections: scala.collection.mutable.HashMap[String, PeerConnection], mediaStream: MediaStream) {

    def updated(evt: Event): State = evt match {
      case PeerConnectionsAdded(pcs) => copy(peerConnection = pcs._1, clusterPeerConnections = pcs._2)
      case MediaStreamAdded(ms) => copy(mediaStream = ms)
    }
  }
}

class Publisher(webRtcHelper: WebRtcHelper, callback: Callback) extends PersistentActor with ActorLogging
  with PeerConnection.Observer {

  val cluster = akka.cluster.Cluster(context.system)

  def members: scala.collection.immutable.SortedSet[akka.cluster.Member] = {
    cluster.state.members.filter(_.status == MemberStatus.Up)
  }

  // self.path.parent.name is the type name (utf-8 URL-encoded)
  // self.path.name is the entry identifier (utf-8 URL-encoded)
  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  // passivate the entity when no activity
//  context.setReceiveTimeout(2.minutes)

  private var state = State(null, null, null)

  private var _identifier: String = null

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
      _identifier = identifier

      val clusterPeerConnections = createRegistryPeerConnections(identifier)
      // Immediately do the offer/answer/candidate dance with all registry members
      for ((path, clusterPeerConnection) <- clusterPeerConnections) {
        val offer = webRtcHelper.createOffer(clusterPeerConnection)
        // TODO: make sure actorSelection is what we actually want
        context.system.actorSelection(path) ! Registry.Internal.Offer(path, offer)
//        context.system.actorSelection(path) ! Registry.Internal.Offer(identifier, offer)
      }

      val peerConnection = webRtcHelper.createPeerConnection(this)

      state = state.updated(PeerConnectionsAdded((peerConnection, clusterPeerConnections)))
      context.become(created)

//      persist(PeerConnectionAdded(pc)) { evt =>
//        state = state.updated(evt)
//        context.become(created)
//        log.info("New PeerConnection created: {}", "id for pc")
//      }
    case _ =>
      println("should not be here")
  }

  def createRegistryPeerConnections(identifier: String): scala.collection.mutable.HashMap[String, PeerConnection] = {
    val clusterPeerConnections = scala.collection.mutable.HashMap.empty[String, PeerConnection]

    for (path <- registryPaths()) {
      log.info(s"Creating PeerConnection for registry at $path")
      val registry = context.system.actorSelection(path)

        val registryObserver = new PeerConnection.Observer() {
          override def onSignalingChange(signalState: SignalingState): Unit = {
            log.info("onSignalingChange called: {}", signalState.name())
          }

          override def onError(): Unit = {
            log.error("onError called...")
          }

          override def onIceCandidate(candidate: IceCandidate): Unit = {
            log.info("onIceCandidate called...")
//            registry ! Internal.Candidate(path, candidate)
            registry ! Internal.Candidate(identifier, candidate)
          }

          override def onRemoveStream(p1: MediaStream): Unit = {
            log.info("onRemoveStream called...")
          }

          override def onIceGatheringChange(gatheringState: IceGatheringState): Unit = {
            log.info("onIceGatheringChange called: [%s]...".format(gatheringState.name()))
          }

          override def onIceConnectionChange(connectionState: IceConnectionState): Unit = {
            log.info("onIceConnectionChange called: {}...", connectionState.name())
          }

          override def onDataChannel(p1: DataChannel): Unit = ???

          override def onAddStream(mediaStream: MediaStream): Unit = {
            log.error("onAddStream called but the registry should never do this!")
          }
        }

      val pc = webRtcHelper.createPeerConnection(registryObserver)
      clusterPeerConnections.put(path, pc)
    }

    clusterPeerConnections
  }

  def registryPaths(): List[String] = {
    members.map(x => registryPath(x.address)).toList
  }

  def registryPath(address: Address): String = {
    s"${address.protocol}://${address.system}@${address.host.get}:${address.port.get}/user/registry"
  }

  def created: Receive = {
    case Incoming.Offer(identifier, sessionDescription) =>
      log.info("Establishing peerConnection for [{}]...", identifier)
      callback.sendAnswer(identifier, webRtcHelper.createAnswer(state.peerConnection, sessionDescription))
    case Incoming.Candidate(identifier, iceCandidate) =>
      log.info("Adding iceCandidate to [{}]...", identifier)
      state.peerConnection.addIceCandidate(iceCandidate)

    case Incoming.Subscribe(identifier: String, targetIdentifier: String) =>
      log.info("wat2")
      log.info("Telling registry to subscribe [{}] to identifier: [{}]", identifier, targetIdentifier)
      context.system.actorSelection("user/registry") ! Incoming.Subscribe(identifier, targetIdentifier)

    // Internal
    case Internal.Candidate(identifier, candidate) =>
      state.clusterPeerConnections.get(identifier).get.addIceCandidate(candidate)
    case Internal.Answer(identifier, answer) =>
      webRtcHelper.setRemoteDescription(state.clusterPeerConnections.get(identifier).get, answer)
    case _ =>
      print("herptidyderpty");
  }

  override def unhandled(msg: Any): Unit = msg match {
    case ReceiveTimeout => context.parent ! Passivate(stopMessage = PoisonPill)
    case _              => super.unhandled(msg)
  }

  override def onSignalingChange(p1: SignalingState): Unit = {
    log.info("onSignalingChange called: {}", p1.name())
  }

  override def onError(): Unit = {
    log.error("onError called...")
  }

  override def onIceCandidate(iceCandidate: IceCandidate): Unit = {
    log.info("onIceCandidate called...")
    callback.sendIceCandidate(_identifier, iceCandidate)
  }

  override def onRemoveStream(p1: MediaStream): Unit = {
    log.info("onRemoveStream called...")
    //    callback.onRemoveStream(sessionId, mediaStream.label())
  }

  override def onIceGatheringChange(p1: IceGatheringState): Unit = {
    log.info("onIceGatheringChange called: [%s]...".format(p1.name()))
  }

  override def onIceConnectionChange(p1: IceConnectionState): Unit = {
    log.info("onIceConnectionChange called: {}...", p1.name())
    if (IceConnectionState.DISCONNECTED.equals(p1)) {
      //      log.debug("Connection [%s] disconnected, un-registering media streams...".format(sessionId))
      //      callback.removeStreamsFromSession(sessionId)
    }
  }

  override def onAddStream(mediaStream: MediaStream): Unit = {
    log.info("onAddStream called, adding mediaStream [{}]", mediaStream.label())
    state = state.updated(MediaStreamAdded(mediaStream))
    val duplicatedMediaStream = webRtcHelper.createDuplicateMediaStream(mediaStream, _identifier)

    for (clusterPeerConnection <- state.clusterPeerConnections) {
      if (clusterPeerConnection._2.addStream(duplicatedMediaStream, webRtcHelper.createConstraints)) {
        log.info("Added duplicated stream [{}] to [{}]", duplicatedMediaStream.label, clusterPeerConnection._1)
      } else {
        log.error("Could not add duplicated stream to [{}]", clusterPeerConnection._1)
      }
    }

    // Add MediaStream to local registry
    context.system.actorSelection("user/registry") ! Registry.Internal.AddMediaStream(_identifier, duplicatedMediaStream)

    callback.onAddStream(_identifier, mediaStream.label())
  }

  override def onDataChannel(p1: DataChannel): Unit = ???

}
