package server

import akka.actor.{ActorLogging, Props}
import akka.contrib.pattern.ShardRegion
import akka.event.LoggingAdapter
import akka.persistence.PersistentActor
import livehq._
import org.webrtc.{IceCandidate, SessionDescription}
import server.Publisher.{Candidate, Offer, RequestPeerConnection}
import server.pc.obs.{PublisherPcObs, PublisherToRegistryPcObs}
import tv.camfire.media.callback.PublisherCallback
import tv.camfire.media.webrtc.WebRtcHelper

import scala.collection.mutable

object Publisher {

//  def props(authorListing: ActorRef): Props =
//    Props(new Publisher(authorListing))
//
//  object PostContent {
//    val empty = PostContent("", "", "")
//  }
//  case class PostContent(author: String, title: String, body: String)
//
//  sealed trait Command {
//    def postId: String
//  }
//  case class AddPost(postId: String, content: PostContent) extends Command
//  case class GetContent(postId: String) extends Command
//  case class ChangeBody(postId: String, body: String) extends Command
//  case class Publish(postId: String) extends Command

  def props(webrtcHelper: WebRtcHelper, callback: PublisherCallback): Props =
    Props(new Publisher(webrtcHelper, callback))

  val idExtractor: ShardRegion.IdExtractor = {
    case cmd: Command => (cmd.identifier, cmd)
    case _ =>
      throw new IllegalStateException("Received unknown message. Cannot resolve destination!");
  }

  val shardResolver: ShardRegion.ShardResolver = msg => msg match {
    case cmd: Command => (math.abs(cmd.identifier.hashCode) % 100).toString
    case _ =>
      throw new IllegalStateException("Received unknown message. Cannot resolve destination!");
  }

  val shardName: String = "Publisher"

//  sealed trait Event
//  case class PostAdded(content: PostContent) extends Event
//  case class BodyChanged(body: String) extends Event
//  case object PostPublished extends Event

//  val idExtractor: ShardRegion.IdExtractor = {
//    case cmd: Command => (cmd.postId, cmd)
//  }
//
//  val shardResolver: ShardRegion.ShardResolver = msg => msg match {
//    case cmd: Command => (math.abs(cmd.postId.hashCode) % 100).toString
//  }


//  private case class State(content: PostContent, published: Boolean) {

//    def updated(evt: Event): State = evt match {
//      case PostAdded(c)   => copy(content = c)
//      case BodyChanged(b) => copy(content = content.copy(body = b))
//      case PostPublished  => copy(published = true)
//    }
//  }

  case class Offer(identifier: String, sessionDescription: SessionDescription) extends Command
  case class Candidate(identifier: String, iceCandidate: IceCandidate) extends Command
  case class Subscribe(identifier: String, publisherIdentifier: String) extends Command

  case class RequestPeerConnection(identifier: String, uuid: String) extends Command
}

  class Publisher(webRtcHelper: WebRtcHelper, callback: PublisherCallback) extends PersistentActor with ActorLogging {

  def _identifier = self.path.name
  val pcId = Log.pcId(_identifier)
  log.info(s"$pcId created.")
  var _incomingPeerConnection: StandardPcDetails = null

  var _registryPeerConnections: mutable.Map[String, PathedPcDetails] = null

  _init()

  private def _init(): Unit = {
    log.info(s"$pcId Initializing...")
    _incomingPeerConnection = new StandardPcDetails(webRtcHelper.createPeerConnection(
      new PublisherPcObs(
        log: LoggingAdapter,
        pcId,
        self,
        callback,
        _identifier
      )), webRtcHelper)
    _registryPeerConnections = mutable.Map.empty[String, PathedPcDetails]
  }

  // self.path.parent.name is the type name (utf-8 URL-encoded) 
  // self.path.name is the entry identifier (utf-8 URL-encoded)
  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

//  // passivate the entity when no activity
//  context.setReceiveTimeout(2.minutes)

//  private var state = State(PostContent.empty, false)

//  override def receiveRecover: Receive = {
//    case evt: PostAdded =>
//      context.become(created)
//      state = state.updated(evt)
//    case evt @ PostPublished =>
//      context.become(published)
//      state = state.updated(evt)
//    case evt: Event => state =
//      state.updated(evt)
//  }

//  override def receiveCommand: Receive = initial
//
//  def initial: Receive = {
//    case GetContent(_) => sender() ! state.content
//    case AddPost(_, content) =>
//      if (content.author != "" && content.title != "")
//        persist(PostAdded(content)) { evt =>
//          state = state.updated(evt)
//          context.become(created)
//          log.info("New post saved: {}", state.content.title)
//        }
//  }
//
//  def created: Receive = {
//    case GetContent(_) => sender() ! state.content
//    case ChangeBody(_, body) =>
//      persist(BodyChanged(body)) { evt =>
//        state = state.updated(evt)
//        log.info("Post changed: {}", state.content.title)
//      }
//    case Publish(postId) =>
//      persist(PostPublished) { evt =>
//        state = state.updated(evt)
//        context.become(published)
//        val c = state.content
//        log.info("Post published: {}", c.title)
//        authorListing ! Subscriber.PostSummary(c.author, postId, c.title)
//      }
//  }
//
//  def published: Receive = {
//    case GetContent(_) => sender() ! state.content
//  }

//  override def unhandled(msg: Any): Unit = msg match {
//    case ReceiveTimeout => context.parent ! Passivate(stopMessage = PoisonPill)
//    case _              => super.unhandled(msg)
//  }
  override def receiveRecover: Receive = {
    case _ =>
      log.warning("recovering message.... not implemented.")
  }

  override def receiveCommand: Receive = {
    // Offer/Candidate are entry points for publishers.
    case Offer(identifier, offer) =>
      log.info(s"$pcId Incoming.Offer received. [${Utils.stripNewline(offer.toString)}")
      val answer = webRtcHelper.createAnswer(_incomingPeerConnection.peerConnection, offer)
      if (answer.isDefined) {
        log.info(s"$pcId Answer created successfully. [${Utils.stripNewline(answer.get.toString)}]")
        callback.sendAnswer(identifier, answer.get)
      } else {
        log.error(s"$pcId Failed to create answer! No answer will be sent back!")
      }
    case Candidate(identifier, iceCandidate) =>
      log.info(s"$pcId Incoming.Candidate received. [${Utils.stripNewline(iceCandidate.toString)}")
      _incomingPeerConnection.peerConnection.addIceCandidate(iceCandidate)

    case Internal.Publisher.AddStream(identifier, mediaStream) =>
      _incomingPeerConnection.addStream(mediaStream)

    case RequestPeerConnection(identifier: String, uuid: String) =>
      log.info(s"Requesting Pc($identifier)($uuid)")
      _initRegistryPc(identifier, uuid)

    // Internal
    case Internal.Candidate(identifier, uuid, candidate) =>
      log.info(s"$pcId Internal.Candidate received for ($uuid). [${Utils.stripNewline(candidate.toString)}]")
      _registryPeerConnections.get(uuid).get.peerConnection.addIceCandidate(candidate)
    case Internal.Answer(identifier, uuid, answer) =>
      log.info(s"$pcId Internal.Answer received for ($uuid). [${Utils.stripNewline(answer.toString)}]")
      webRtcHelper.setRemoteDescription(_registryPeerConnections.get(uuid).get.peerConnection, answer)

    case Internal.CleanRegistryPeerConnections(identifier) =>
      log.info(s"$pcId Disconnected, closing [${_registryPeerConnections.size}] registry PeerConnection(s)...")
      _registryPeerConnections.foreach {
        case (uuid, pcDetail) => {
          log.info(s"$pcId Closing $uuid...")
          log.info(s"$pcId ($uuid) Closing ${pcDetail.getMediaStreams.size} MediaStreams")
          pcDetail.getMediaStreams.foreach {
            case (label, mediaStream) => {
              pcDetail.peerConnection.removeStream(mediaStream)
              // Even though we remove the stream, the onRemoveStream callback does not seem to automatically get called
              callback.onRemoveStream(_identifier, mediaStream)
            }
          }
          pcDetail.mMediaStreams.clear()
          pcDetail.peerConnection.close()
        }
      }
      // TODO: We shouldn't re-init here. Instead we should become a state that doesn't accept new messages!
      _init()

//    case Internal.AddRegistryMediaStream(mediaStreamId, mediaStream) =>
//      log.info(s"$pcId Internal.AddMediaStream : Adding MediaStream($mediaStreamId)...")
//      _incomingPeerConnection.peerConnection.addStream(mediaStream, webRtcHelper.createConstraints)
//
//      // Update offer
//      val offer = webRtcHelper.createOffer(_incomingPeerConnection.peerConnection)
//      if (offer.isDefined) {
//        log.info(s"Added MediaStream(${mediaStream.label()}). Sending updated offer.")
//        callback.sendOffer(_identifier, offer.get)
//      } else {
//        log.error(s"Added MediaStream(${mediaStream.label()}. but failed to create offer! No offer will be sent!")
//      }

    case _ =>
      log.info("Received unknown message!")
  }

  def _initRegistryPc(identifier: String, uuid: String): Unit = {
    val actorRef = sender()
    val path = actorRef.path.toString
    val logId = Log.registryPubPcId(_identifier, uuid)
    log.info(s"$logId Creating registry PeerConnection at [$path]")
    callback.onRegistryPubInitialize(identifier, uuid, path)

    val registryObserver = new PublisherToRegistryPcObs(log, logId, context.system, path, self, callback,
      identifier, uuid)

    val pc = webRtcHelper.createPeerConnection(registryObserver)

    // Attach all MediaStreams to registry PeerConnection
    val mediaStreams = _incomingPeerConnection.getDuplicatedMediaStreams
    log.info(s"$logId Attaching [${mediaStreams.size}] MediaStreams...")
    mediaStreams.foreach {
      case (mediaStreamId, mediaStream) => {
        log.info(s"$logId Adding MediaStream(${mediaStream.label()})")
        if (pc.addStream(mediaStream)) {
          log.info(s"$logId Successfully Added MediaStream(${mediaStream.label()})")
        } else {
          log.error(s"$logId Failed to add MediaStream(${mediaStream.label()})")
        }
      }
    }

    val offer = webRtcHelper.createOffer(pc)
    if (offer.isDefined) {
      log.info(s"$logId Sending offer to registry...")
      actorRef ! Internal.Offer(identifier, uuid, offer.get)
    } else {
      log.error(s"$logId Failed to create an offer! No offer will be sent!")
    }

    _registryPeerConnections.put(uuid, new PathedPcDetails(path, pc, webRtcHelper))
  }
}
