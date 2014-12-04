package server

import akka.actor._
import akka.cluster.MemberStatus
import akka.contrib.pattern.ShardRegion
import livehq._
import org.webrtc.PeerConnection.{Observer, IceConnectionState, IceGatheringState, SignalingState}
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
with ActorLogging {
  val pcId = Log.pcId(_identifier)
  log.info(s"$pcId created.")
  var _incomingPeerConnection: StandardPcDetails = null

  var _registryPeerConnections: mutable.Map[String, PathedPcDetails] = null

  def _init(): Unit = {
    log.info(s"$pcId (re)initializing...")
    _incomingPeerConnection = new StandardPcDetails(webRtcHelper.createPeerConnection(createPcObserver()))
    _registryPeerConnections = mutable.Map.empty[String, PathedPcDetails]
  }

  _init()

  def createPcObserver(): PeerConnection.Observer = {
    new Observer {
      override def onError(): Unit = {
        log.error(s"$pcId.onError.")
      }

      override def onIceCandidate(iceCandidate: IceCandidate): Unit = {
        log.info(s"$pcId.onIceCandidate.")
        callback.sendIceCandidate(_identifier, iceCandidate)
      }

      override def onRemoveStream(mediaStream: MediaStream): Unit = {
        log.info(s"$pcId.onRemoveStream : [${mediaStream.label()}.")
        callback.onRemoveStream(_identifier, mediaStream)
      }

      override def onIceGatheringChange(gatheringState: IceGatheringState): Unit = {
        log.info(s"$pcId.onIceGatheringChange : [${gatheringState.name()}].")
        callback.onIceGatheringChange(_identifier, gatheringState)
      }

      override def onSignalingChange(signalState: SignalingState): Unit = {
        log.info(s"$pcId.onSignalingChange : [${signalState.name()}].")
        callback.onSignalingChange(_identifier, signalState)
      }

      override def onIceConnectionChange(iceConnectionState: IceConnectionState): Unit = {
        log.info(s"$pcId.onIceConnectionChange : [${iceConnectionState.name()}].")
        callback.onIceConnectionChange(_identifier, iceConnectionState)
        if (iceConnectionState == IceConnectionState.CONNECTED) {
          // We have the actor send a message to itself because it will fail here if we try to have a PeerConnection
          // create an offer. Not sure exactly why this is the case but might be a threading issue since we are dealing
          // with an actor.
          self.tell(Internal.CreateRegistryPeerConnections(_identifier), self)
//          self ! Internal.CreateRegistryPeerConnections(_identifier)
        } else if(iceConnectionState == IceConnectionState.DISCONNECTED) {
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
        }
      }

      override def onAddStream(mediaStream: MediaStream): Unit = {
        log.info(s"$pcId.onAddStream : [${mediaStream.label()}]")
        callback.onAddStream(_identifier, mediaStream)

        _incomingPeerConnection.addStream(mediaStream)

        val duplicatedMediaStream = webRtcHelper.createDuplicateMediaStream(mediaStream, _identifier)

        log.info(s"$pcId Adding MediaStream(${mediaStream.label()}) to [${_registryPeerConnections.size}] registry PeerConnection(s)")
        _registryPeerConnections.foreach {
          case (uuid, pcDetail) => {
            if (pcDetail.peerConnection.addStream(duplicatedMediaStream, webRtcHelper.createConstraints)) {
              log.info(s"$pcId ($uuid) added duplicated stream : ${duplicatedMediaStream.label}")

              val offer = webRtcHelper.createOffer(pcDetail.peerConnection)
              if (offer.isDefined) {
                context.system.actorSelection(pcDetail.path).tell(offer.get, self)
//                context.system.actorSelection(pcDetail.path) ! offer.get
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
    }
  }

  override def receive: Actor.Receive = {
    case Incoming.Offer(identifier, sessionDescription) =>
      log.info(s"$pcId Incoming.Offer received.")
      val answer = webRtcHelper.createAnswer(_incomingPeerConnection.peerConnection, sessionDescription)
      if (answer.isDefined) {
        callback.sendAnswer(identifier, answer.get)
      } else {
        log.error(s"$pcId Failed to create answer! No answer will be sent back!")
      }
    case Incoming.Candidate(identifier, iceCandidate) =>
      log.info(s"$pcId Incoming.Candidate received.")
      _incomingPeerConnection.peerConnection.addIceCandidate(iceCandidate)
    case Incoming.Subscribe(identifier: String, targetIdentifier: String) =>
      log.info(s"$pcId Incoming.Subscribe received ($identifier -> $targetIdentifier)")
      // This tells the registry to return the MediaStream
      context.system.actorSelection("user/registry") ! Incoming.Subscribe(identifier, targetIdentifier)

    // Internal
    case Internal.Candidate(identifier, uuid, candidate) =>
      log.info(s"$pcId Internal.Candidate received ($uuid).")
      _registryPeerConnections.get(uuid).get.peerConnection.addIceCandidate(candidate)
    case Internal.Answer(identifier, uuid, answer) =>
      log.info(s"$pcId Internal.Answer received ($uuid).")
      webRtcHelper.setRemoteDescription(_registryPeerConnections.get(uuid).get.peerConnection, answer)
    case Internal.CreateRegistryPeerConnections(identifier) =>
      log.info(s"$pcId Internal.CreateRegistryPeerConnections received.")
      _initializeRegistryPeerConnections(_identifier)
    case Internal.AttachMediaStreams(identifier, uuid) =>
      log.info(s"$pcId Internal.AttachMediaStreams received.")
      val registryPcDetails = _registryPeerConnections.get(uuid).get

      for (mediaStream <- _incomingPeerConnection.getMediaStreams) {
        registryPcDetails.peerConnection.addStream(mediaStream._2, webRtcHelper.createConstraints)
      }

      // Send out an updated offer after MediaStream(s) have been added.
      val offer = webRtcHelper.createOffer(registryPcDetails.peerConnection)
      if (offer.isDefined) {
        context.system.actorSelection(registryPcDetails.path) ! Internal.Offer(identifier, uuid, offer.get)
      } else {
        log.error(s"$pcId Failed to create an offer for [$uuid]! No offer will be sent!")
      }

    case Internal.AddMediaStream(identifier, mediaStreamId, mediaStream) =>
      log.info(s"$pcId Internal.AddMediaStream : Adding MediaStream($mediaStreamId)...")
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

  def registryPaths(): List[String] = {
//    members.map(x => x.roles.map(y => log.info(s"DOH: $y")))

    members.map(x => registryPath(x.address)).toList
  }

  def registryPath(address: Address): String = {
    s"${address.protocol}://${address.system}@${address.host.get}:${address.port.get}/user/registry"
  }

  val cluster = akka.cluster.Cluster(context.system)

  def members: scala.collection.immutable.SortedSet[akka.cluster.Member] = {
    cluster.state.members.filter(_.status == MemberStatus.Up)
  }

  def _identifier = self.path.name

  def newUuid = java.util.UUID.randomUUID.toString

  def _initializeRegistryPeerConnections(identifier: String): Unit = {
    log.info(s"$pcId Creating registry PeerConnections...")
    for (path: String <- registryPaths()) {
      val uuid = newUuid
      val logId = Log.registryPubPcId(_identifier, uuid)
      log.info(s"$logId Creating registry PeerConnection at [$path]")
      callback.onRegistryPubInitialize(identifier, uuid, path)

      val registry = context.system.actorSelection(path)

      val registryObserver = new PeerConnection.Observer() {
        override def onSignalingChange(signalState: SignalingState): Unit = {
          log.info(s"$logId.onSignalingChange : [${signalState.name()}].")
          callback.onRegistryPubSignalingChange(identifier, uuid, signalState)
        }

        override def onError(): Unit = {
          log.error(s"$logId.onError!")
        }

        override def onIceCandidate(candidate: IceCandidate): Unit = {
          log.info(s"$logId.onIceCandidate [${candidate.toString}]")
          registry ! Internal.Candidate(identifier, uuid, candidate)
        }

        override def onIceGatheringChange(gatheringState: IceGatheringState): Unit = {
          log.info(s"$logId.onIceGatheringChange : [${gatheringState.name()}].")
          callback.onRegistryPubIceGatheringChange(identifier, uuid, gatheringState)
        }

        override def onIceConnectionChange(iceConnectionState: IceConnectionState): Unit = {
          log.info(s"$logId.onIceConnectionChange : [${iceConnectionState.name()}].")
          callback.onRegistryPubIceConnectionChange(identifier, uuid, iceConnectionState)
          if (iceConnectionState == IceConnectionState.CONNECTED) {
            // Now that we're connected to the registry PeerConnection, we can add any MediaStreams
            log.info(s"$logId Attaching MediaStreams.")

            self ! Internal.AttachMediaStreams(identifier, uuid)
          }
        }

        // Registry PeerConnections(pub) are only outgoing! It would never make sense to have a Registry PeerConnection
        // ever send anything back to the Publisher since the registry only acts as a means of replication!
        override def onAddStream(mediaStream: MediaStream): Unit = {
          log.error(s"$logId.onAddStream : [${mediaStream.label()}] called but the registry should never do this!")
        }

        // onAddStream should never get called, thus onRemove will never get called either!
        override def onRemoveStream(mediaStream: MediaStream): Unit = {
          log.error(s"$logId.onRemoveStream : [${mediaStream.label()}.")
        }

        override def onDataChannel(p1: DataChannel): Unit = ???
      }

      val pc = webRtcHelper.createPeerConnection(registryObserver)

      // Attach all MediaStream to registry PeerConnection
      val mediaStreams = _incomingPeerConnection.getMediaStreams
      log.info(s"$logId Attaching [${mediaStreams.size}] MediaStreams...")
      mediaStreams.foreach {
        case (mediaStreamId, mediaStream) => {
          log.info(s"$logId Adding MediaStream(${mediaStream.label()})")
          pc.addStream(mediaStream, webRtcHelper.createConstraints)
          val offer = webRtcHelper.createOffer(pc)
          if (offer.isDefined) {
            log.info(s"$logId Added MediaStream(${mediaStream.label()}, sending updated offer.")
            callback.sendOffer(identifier, offer.get)
          } else {
            log.error(s"$logId Added MediaStream(${mediaStream.label()}, but failed to create offer! No offer will be sent!")
          }
        }
      }

      _registryPeerConnections.put(uuid, new PathedPcDetails(path, pc))

      // Do the offer/answer/candidate dance with all registry members
      val offer = webRtcHelper.createOffer(pc)
      if (offer.isDefined) {
        context.system.actorSelection(path) ! Internal.Offer(identifier, uuid, offer.get)
      } else {
        log.error(s"$logId Failed to create an offer! No offer will be sent!")
      }
    }
  }
}
