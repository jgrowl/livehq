package server

import akka.actor._
import akka.contrib.pattern.ShardRegion
import akka.event.LoggingAdapter
import livehq._
import server.pc.obs.{PublisherToRegistryPcObs, PublisherPcObs}
import tv.camfire.media.callback.Callback
import tv.camfire.media.webrtc.WebRtcHelper

import scala.collection.mutable

object Publisher {
  def props(webrtcHelper: WebRtcHelper, callback: Callback): Props =
    Props(new Publisher(webrtcHelper, callback))

  val idExtractor: ShardRegion.IdExtractor = {
    case cmd: Command => (cmd.identifier, cmd)
  }

  val shardResolver: ShardRegion.ShardResolver = msg => msg match {
    case cmd: Command => (math.abs(cmd.identifier.hashCode) % 100).toString
  }

  val shardName: String = "Publisher"
}

class Publisher(webRtcHelper: WebRtcHelper, callback: Callback) extends Actor
with ActorLogging {
  val pcId = Log.pcId(_identifier)
  log.info(s"$pcId created.")
  var _incomingPeerConnection: StandardPcDetails = null

  var _registryPeerConnections: mutable.Map[String, PathedPcDetails] = null

  _init()

  private def _init(): Unit = {
    log.info(s"$pcId Initialized.")
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

  override def receive: Actor.Receive = {
    // Offer/Candidate are entry points for publishers.
    case Incoming.Offer(identifier, offer) =>
      log.info(s"$pcId Incoming.Offer received. [${Utils.stripNewline(offer.toString)}")
      val answer = webRtcHelper.createAnswer(_incomingPeerConnection.peerConnection, offer)
      if (answer.isDefined) {
        log.info(s"$pcId Answer created successfully. [${Utils.stripNewline(answer.get.toString)}]")
        callback.sendAnswer(identifier, answer.get)
      } else {
        log.error(s"$pcId Failed to create answer! No answer will be sent back!")
      }
    case Incoming.Candidate(identifier, iceCandidate) =>
      log.info(s"$pcId Incoming.Candidate received. [${Utils.stripNewline(iceCandidate.toString)}")
      _incomingPeerConnection.peerConnection.addIceCandidate(iceCandidate)

    case Internal.Publisher.AddStream(identifier, mediaStream) =>
      _incomingPeerConnection.addStream(mediaStream)

    case Incoming.Subscribe(identifier: String, publisherIdentifier: String) =>
      log.info(s"$pcId Incoming.Subscribe received. ($identifier -> $publisherIdentifier)")
      // This tells the registry to return the MediaStream
      context.system.actorSelection("user/registry") ! Incoming.Subscribe(identifier, publisherIdentifier)

    case Internal.RequestPeerConnection(identifier: String, uuid: String) =>
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
      _init()

//    case Internal.AddRegistryMediaStream(identifier, mediaStreamId, mediaStream) =>
    case Internal.AddRegistryMediaStream(mediaStreamId, mediaStream) =>
      log.info(s"$pcId Internal.AddMediaStream : Adding MediaStream($mediaStreamId)...")
      _incomingPeerConnection.peerConnection.addStream(mediaStream, webRtcHelper.createConstraints)

      // Update offer
      val offer = webRtcHelper.createOffer(_incomingPeerConnection.peerConnection)
      if (offer.isDefined) {
        log.info(s"Added MediaStream(${mediaStream.label()}). Sending updated offer.")
        callback.sendOffer(_identifier, offer.get)
      } else {
        log.error(s"Added MediaStream(${mediaStream.label()}. but failed to create offer! No offer will be sent!")
      }
  }

  def _identifier = self.path.name

  def _initRegistryPc(identifier: String, uuid: String): Unit = {
    val actorRef = sender()
    val path = actorRef.path.toString
    val logId = Log.registryPubPcId(_identifier, uuid)
    log.info(s"$logId Creating registry PeerConnection at [$path]")
    callback.onRegistryPubInitialize(identifier, uuid, path)

    //      val registry = context.system.actorSelection(path)
    val registryObserver = new PublisherToRegistryPcObs(log, logId, context.system, path, self, callback,
      identifier, uuid)

    val pc = webRtcHelper.createPeerConnection(registryObserver)

    // Attach all MediaStreams to registry PeerConnection
    val mediaStreams = _incomingPeerConnection.getDuplicatedMediaStreams
    log.info(s"$logId Attaching [${mediaStreams.size}] MediaStreams...")
    mediaStreams.foreach {
      case (mediaStreamId, mediaStream) => {
        log.info(s"$logId Adding MediaStream(${mediaStream.label()})")
        if (pc.addStream(mediaStream, webRtcHelper.createConstraints)) {
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
