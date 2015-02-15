package server

import akka.actor.{ActorLogging, Props}
import akka.contrib.pattern.ShardRegion
import akka.event.LoggingAdapter
import akka.persistence.PersistentActor
import livehq._
import org.webrtc.{MediaStream, IceCandidate, SessionDescription}
import server.pc.obs.SubscriberPcObs
import server.registry.Registry
import tv.camfire.media.callback.SubscriberCallback
import tv.camfire.media.webrtc.WebRtcHelper

object Subscriber {

  def props(webrtcHelper: WebRtcHelper, callback: SubscriberCallback): Props =
    Props(new Subscriber(webrtcHelper, callback))

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

  val shardName: String = "Subscriber"

  case class Subscribe(identifier: String, publisherIdentifier: String) extends Command

  case class Answer(identifier: String, answer: SessionDescription) extends Command

  case class Candidate(identifier: String, candidate: IceCandidate) extends Command
}

class Subscriber(webRtcHelper: WebRtcHelper, subscriberCallback: SubscriberCallback) extends PersistentActor with ActorLogging {

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  val pcId = Log.pcId(_identifier)
  log.info(s"$pcId created.")
  val _incomingPeerConnection = new StandardPcDetails(webRtcHelper.createPeerConnection(
    new SubscriberPcObs(
      log: LoggingAdapter,
      pcId,
      self,
      subscriberCallback,
      _identifier
    )), webRtcHelper)


  //  // passivate the entity when no activity
  //  context.setReceiveTimeout(2.minutes)
  //
  //  var posts = Vector.empty[PostSummary]
  //
  //  def receiveCommand = {
  //    case s: PostSummary =>
  //      persist(s) { evt =>
  //        posts :+= evt
  //        log.info("Post added to {}'s list: {}", s.author, s.title)
  //      }
  //    case GetPosts(_) =>
  //      sender() ! Posts(posts)
  //    case ReceiveTimeout => context.parent ! Passivate(stopMessage = PoisonPill)
  //  }
  //
  //  override def receiveRecover: Receive = {
  //    case evt: PostSummary => posts :+= evt
  //
  //  }
  override def receiveRecover: Receive = {
    case _ =>
      println("not implemented")
  }

  override def receiveCommand: Receive = {
    case Subscriber.Subscribe(identifier, publisherIdentifier) =>
      log.info(s"$pcId Subscribe received. ($identifier -> $publisherIdentifier)")
      context.system.actorSelection("user/registry") ! Registry.Incoming.Subscribe(publisherIdentifier)

//    case Subscriber.Offer(identifier, offer) =>
//      log.info(s"$pcId Offer received. [${Utils.stripNewline(offer.toString)}")
//      val answer = webRtcHelper.createAnswer(_incomingPeerConnection.peerConnection, offer)
//      if (answer.isDefined) {
//        log.info(s"$pcId Answer created successfully. [${Utils.stripNewline(answer.get.toString)}]")
//        subscriberCallback.sendAnswer(identifier, answer.get)
//      } else {
//        log.error(s"$pcId Failed to create answer! No answer will be sent back!")
//      }

    case Subscriber.Answer(identifier, answer) =>
      log.info(s"$pcId Answer received. [${Utils.stripNewline(answer.toString)}")
      webRtcHelper.setRemoteDescription(_incomingPeerConnection.peerConnection, answer)

//      val answer = webRtcHelper.createAnswer(_incomingPeerConnection.peerConnection, answer)
//      if (answer.isDefined) {
//        log.info(s"$pcId Answer created successfully. [${Utils.stripNewline(answer.get.toString)}]")
//        subscriberCallback.sendAnswer(identifier, answer.get)
//      } else {
//        log.error(s"$pcId Failed to create answer! No answer will be sent back!")
//      }

    case Subscriber.Candidate(identifier, candidate) =>
      log.info(s"$pcId Candidate received. [${Utils.stripNewline(candidate.toString)}")
      _incomingPeerConnection.peerConnection.addIceCandidate(candidate)

//    case Internal.Publisher.AddStream(identifier, mediaStream) =>
//      log.info(s"Adding MediaStream2 $identifier.")
//      _incomingPeerConnection.addStream(mediaStream)


    case Internal.AddRegistryMediaStream(mediaStreamId, mediaStream) =>
      log.info(s"$pcId Internal.AddMediaStream : Adding MediaStream($mediaStreamId)...")
      _incomingPeerConnection.peerConnection.addStream(mediaStream, webRtcHelper.createConstraints)

      // Update offer
      val offer = webRtcHelper.createOffer(_incomingPeerConnection.peerConnection)
      if (offer.isDefined) {
        log.info(s"Added MediaStream(${mediaStream.label()}). Sending updated offer.")
        subscriberCallback.sendOffer(_identifier, offer.get)
      } else {
        log.error(s"Added MediaStream(${mediaStream.label()}. but failed to create offer! No offer will be sent!")
      }

//    // Internal
//    case Internal.Candidate(identifier, uuid, candidate) =>
//      log.info(s"$pcId Internal.Candidate received for ($uuid). [${Utils.stripNewline(candidate.toString)}]")
//      _registryPeerConnections.get(uuid).get.peerConnection.addIceCandidate(candidate)
//    case Internal.Answer(identifier, uuid, answer) =>
//      log.info(s"$pcId Internal.Answer received for ($uuid). [${Utils.stripNewline(answer.toString)}]")
//      webRtcHelper.setRemoteDescription(_registryPeerConnections.get(uuid).get.peerConnection, answer)

    //
    //        case Internal.CleanRegistryPeerConnections(identifier) =>
    //          log.info(s"$pcId Disconnected, closing [${_registryPeerConnections.size}] registry PeerConnection(s)...")
    //          _registryPeerConnections.foreach {
    //            case (uuid, pcDetail) => {
    //              log.info(s"$pcId Closing $uuid...")
    //              log.info(s"$pcId ($uuid) Closing ${pcDetail.getMediaStreams.size} MediaStreams")
    //              pcDetail.getMediaStreams.foreach {
    //                case (label, mediaStream) => {
    //                  pcDetail.peerConnection.removeStream(mediaStream)
    //                  // Even though we remove the stream, the onRemoveStream callback does not seem to automatically get called
    //                  callback.onRemoveStream(_identifier, mediaStream)
    //                }
    //              }
    //              pcDetail.mMediaStreams.clear()
    //              pcDetail.peerConnection.close()
    //            }
    //          }
    //          _init()
    //

    case command: PeerConnectionCommand =>
      log.error(command.toString);

    case command: Command =>
      log.error(command.toString);

    case _ =>
      log.warning("Received unknown message!")


  }

  def _identifier = self.path.name
}