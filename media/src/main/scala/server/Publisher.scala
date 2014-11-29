package server

import akka.actor._
import akka.cluster.MemberStatus
import akka.contrib.pattern.ShardRegion
import livehq._
import org.webrtc.PeerConnection.{IceConnectionState, IceGatheringState, SignalingState}
import org.webrtc._
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
with ActorLogging
with PeerConnection.Observer {
  log.info(s"Creating PeerConnection(${_identifier}.")
  private val _incomingPeerConnection = new StandardPcDetails(webRtcHelper.createPeerConnection(this))

  private val _registryPeerConnections = mutable.Map.empty[String, PathedPcDetails]

  override def receive: Actor.Receive = {
    case Incoming.Offer(identifier, sessionDescription) =>
      log.info(s"Incoming.Offer received.")
      val answer = webRtcHelper.createAnswer(_incomingPeerConnection.peerConnection, sessionDescription)
      if (answer.isDefined) {
        callback.sendAnswer(identifier, answer.get)
      } else {
        log.error("Failed to create answer! No answer will be sent back!")
      }
    case Incoming.Candidate(identifier, iceCandidate) =>
      log.info(s"Incoming.Candidate received.")
      _incomingPeerConnection.peerConnection.addIceCandidate(iceCandidate)

    case Incoming.Subscribe(identifier: String, targetIdentifier: String) =>
      log.info(s"Incoming.Subscribe received ($identifier -> $targetIdentifier)")
      // This tells the registry to return the MediaStream
      context.system.actorSelection("user/registry") ! Incoming.Subscribe(identifier, targetIdentifier)

    // Internal
    case Internal.Candidate(identifier, uuid, candidate) =>
      log.info(s"Internal.Candidate received ($uuid).")
      _registryPeerConnections.get(uuid).get.peerConnection.addIceCandidate(candidate)
    case Internal.Answer(identifier, uuid, answer) =>
      log.info(s"Internal.Answer received ($uuid).")
      webRtcHelper.setRemoteDescription(_registryPeerConnections.get(uuid).get.peerConnection, answer)

    case Internal.CreateRegistryPeerConnections(identifier) =>
      log.info(s"Internal.CreateRegistryPeerConnections received.")
      //      // TODO: I figure if we get a CONNECTED, then we should assume we need to always reconnect to all registry members
      //      // from scratch. Seems like we should just clear them out before doing anything. I need to evaluate whether this
      //      // is a good idea.
      //      _registryPeerConnections.clear()

      _initializeRegistryPeerConnections(_identifier)

    case Internal.AttachMediaStreams(identifier, uuid) =>
      log.info(s"Internal.AttachMediaStreams received.")
      val registryPcDetails = _registryPeerConnections.get(uuid).get

      for (mediaStream <- _incomingPeerConnection.getMediaStreams) {
        registryPcDetails.peerConnection.addStream(mediaStream._2, webRtcHelper.createConstraints)
      }

      // Send out an updated offer after MediaStream(s) have been added.
      val offer = webRtcHelper.createOffer(registryPcDetails.peerConnection)
      if (offer.isDefined) {
        context.system.actorSelection(registryPcDetails.path) ! Internal.Offer(identifier, uuid, offer.get)
      } else {
        log.error(s"($uuid) Failed to create an offer! No offer will be sent!")
      }

    case Internal.AddMediaStream(identifier, mediaStreamId, mediaStream) =>
      log.info(s"Internal.AddMediaStream : Adding MediaStream($mediaStreamId)...")
      // TODO: Figure out if we can duplicate the stream just once per publisher and registry nodes
      // We need to duplicate the media stream again.
      webRtcHelper.createDuplicateMediaStream(mediaStream, mediaStreamId + "-1")
      _incomingPeerConnection.peerConnection.addStream(mediaStream, webRtcHelper.createConstraints)

      // Update offer
      val offer = webRtcHelper.createOffer(_incomingPeerConnection.peerConnection)
      if (offer.isDefined) {
        log.info(s"Added MediaStream(${mediaStream.label()}). Sending updated offer.")
        callback.sendOffer(identifier, offer.get)
      } else {
        log.error(s"Added MediaStream(${mediaStream.label()}. but failed to create offer! No offer will be sent!")
      }
  }

  override def onSignalingChange(signalState: SignalingState): Unit = {
    log.info(s"onSignalingChange : [${signalState.name()}].")
    callback.onSignalingChange(_identifier, signalState)
  }

  override def onError(): Unit = {
    log.error(s"onError.")
  }

  override def onIceCandidate(iceCandidate: IceCandidate): Unit = {
    log.info(s"onIceCandidate.")
    callback.sendIceCandidate(_identifier, iceCandidate)
  }

  override def onRemoveStream(mediaStream: MediaStream): Unit = {
    log.info(s"onRemoveStream : [${mediaStream.label()}.")
    callback.onRemoveStream(_identifier, mediaStream)
  }

  override def onIceGatheringChange(gatheringState: IceGatheringState): Unit = {
    log.info(s"onIceGatheringChange : [${gatheringState.name()}].")
    callback.onIceGatheringChange(_identifier, gatheringState)
  }

  override def onIceConnectionChange(iceConnectionState: IceConnectionState): Unit = {
    log.info(s"onIceConnectionChange : [${iceConnectionState.name()}].")
    callback.onIceConnectionChange(_identifier, iceConnectionState)
    if (iceConnectionState == IceConnectionState.CONNECTED) {
      // We have the actor send a message to itself because it will fail here if we try to have a PeerConnection
      // create an offer. Not sure exactly why this is the case but might be a threading issue since we are dealing
      // with an actor.
      self ! Internal.CreateRegistryPeerConnections(_identifier)
    } else if(iceConnectionState == IceConnectionState.DISCONNECTED) {
      log.info(s"Disconnected, closing [${_registryPeerConnections.size}] registry PeerConnection(s)...")
      _registryPeerConnections.foreach {
        case (uuid, pcDetail) => {
          log.info(s"Closing $uuid...")
          log.info(s"(${uuid}) Closing ${pcDetail.getMediaStreams.size} MediaStreams")
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
      _registryPeerConnections.clear()
    }
  }

  override def onAddStream(mediaStream: MediaStream): Unit = {
    log.info(s"onAddStream : [${mediaStream.label()}]")
    callback.onAddStream(_identifier, mediaStream)

    _incomingPeerConnection.addStream(mediaStream)

    val duplicatedMediaStream = webRtcHelper.createDuplicateMediaStream(mediaStream, _identifier)

    log.info(s"Adding MediaStream(${mediaStream.label()}) to [${_registryPeerConnections.size}] registry PeerConnection(s)")
    _registryPeerConnections.foreach {
      case (uuid, pcDetail) => {
        if (pcDetail.peerConnection.addStream(duplicatedMediaStream, webRtcHelper.createConstraints)) {
          log.info(s"($uuid) added duplicated stream : ${duplicatedMediaStream.label}")

          val offer = webRtcHelper.createOffer(pcDetail.peerConnection)
          if (offer.isDefined) {
            context.system.actorSelection(pcDetail.path) ! offer.get
          } else {
            log.error(s"($uuid) Failed to create offer! No offer will be sent!")
          }
        } else {
          log.error(s"($uuid) could not add duplicated stream : ${duplicatedMediaStream.label}")
        }
      }
    }
  }

  override def onDataChannel(p1: DataChannel): Unit = ???

  def registryPaths(): List[String] = {
    members.map(x => registryPath(x.address)).toList
  }

  def registryPath(address: Address): String = {
    s"${address.protocol}://${address.system}@${address.host.get}:${address.port.get}/user/registry"
  }

  val cluster = akka.cluster.Cluster(context.system)

  def members: scala.collection.immutable.SortedSet[akka.cluster.Member] = {
    cluster.state.members.filter(_.status == MemberStatus.Up)
  }

  // TODO: Not sure if this will always work
  def _identifier = self.path.name

  def newUuid = java.util.UUID.randomUUID.toString

  def _initializeRegistryPeerConnections(identifier: String): Unit = {
    for (path: String <- registryPaths()) {
      val uuid = newUuid
      log.info(s"Creating registry PeerConnection($uuid) for [$path]")
      callback.onRegistryPubInitialize(identifier, uuid, path)

      val registry = context.system.actorSelection(path)

      val registryObserver = new PeerConnection.Observer() {
        override def onSignalingChange(signalState: SignalingState): Unit = {
          log.info(s"RegistryPeerConnection($uuid).onSignalingChange : [${signalState.name()}].")
          callback.onRegistryPubSignalingChange(identifier, uuid, signalState)
        }

        override def onError(): Unit = {
          log.error(s"RegistryPeerConnection($uuid).onError!")
        }

        override def onIceCandidate(candidate: IceCandidate): Unit = {
          log.info(s"RegistryPeerConnection($uuid).onIceCandidate [${candidate.toString}]")
          registry ! Internal.Candidate(identifier, uuid, candidate)
        }

        override def onRemoveStream(mediaStream: MediaStream): Unit = {
          log.info(s"RegistryPeerConnection($uuid).onRemoveStream : [${mediaStream.label()}.")
          callback.onRegistryPubRemoveStream(identifier, uuid, mediaStream)
        }

        override def onIceGatheringChange(gatheringState: IceGatheringState): Unit = {
          log.info(s"RegistryPeerConnection($uuid).onIceGatheringChange : [${gatheringState.name()}].")
          callback.onRegistryPubIceGatheringChange(identifier, uuid, gatheringState)
        }

        override def onIceConnectionChange(iceConnectionState: IceConnectionState): Unit = {
          log.info(s"RegistryPeerConnection($uuid).onIceConnectionChange : [${iceConnectionState.name()}].")
          callback.onRegistryPubIceConnectionChange(identifier, uuid, iceConnectionState)
          if (iceConnectionState == IceConnectionState.CONNECTED) {
            // Now that we're connected to the registry PeerConnection, we can add any MediaStreams
            log.info(s"RegistryPeerConnection($uuid) Attaching MediaStreams.")

            self ! Internal.AttachMediaStreams(identifier, uuid)
          }
        }

        override def onAddStream(mediaStream: MediaStream): Unit = {
          log.info(s"RegistryPeerConnection($uuid).onAddStream : [${mediaStream.label()}]")
          // TODO: This should actually happen!?
          log.error("onAddStream called but the registry should never do this!")
          callback.onRegistryPubAddStream(identifier, uuid, mediaStream)
        }

        override def onDataChannel(p1: DataChannel): Unit = ???
      }

      val pc = webRtcHelper.createPeerConnection(registryObserver)

      // Attach all MediaStream to registry PeerConnection
      val mediaStreams = _incomingPeerConnection.getMediaStreams
      log.info(s"Attaching [${mediaStreams.size}] MediaStreams...")
      mediaStreams.foreach {
        case (mediaStreamId, mediaStream) => {
          log.info(s"Adding MediaStream(${mediaStream.label()}) to registry PeerConnection($uuid)")
          pc.addStream(mediaStream, webRtcHelper.createConstraints)
          val offer = webRtcHelper.createOffer(pc)
          if (offer.isDefined) {
            log.info(s"Added MediaStream(${mediaStream.label()}. Sending updated offer.")
            callback.sendOffer(identifier, offer.get)
          } else {
            log.error(s"Added MediaStream(${mediaStream.label()}. but failed to create offer! No offer will be sent!")
          }
        }
      }

      _registryPeerConnections.put(uuid, new PathedPcDetails(path, pc))

      // Do the offer/answer/candidate dance with all registry members
      val offer = webRtcHelper.createOffer(pc)
      if (offer.isDefined) {
        context.system.actorSelection(path) ! Internal.Offer(identifier, uuid, offer.get)
      } else {
        log.error(s"($uuid) Failed to create an offer! No offer will be sent!")
      }
    }
  }
}
