package tv.camfire.actor

import akka.actor.{ActorSystem, ActorRef, Actor, ActorLogging}
import akka.contrib.pattern.ClusterSharding
import akka.event.LoggingAdapter
import livehq._
import org.webrtc.PeerConnection.{IceConnectionState, IceGatheringState, SignalingState}
import org.webrtc.{DataChannel, IceCandidate, MediaStream, PeerConnection}
import server.Publisher
import server.pc.obs.RegistryToPublisherPcObs
import tv.camfire.media.callback.Callback
import tv.camfire.media.webrtc.WebRtcHelper

import scala.collection.mutable

/**
 * User: jonathan
 * Date: 5/2/13
 * Time: 8:06 PM
 */
class Registry(webRtcHelper: WebRtcHelper, callback: Callback) extends Actor with ActorLogging {

  // Map identifiers to a PcDetails, not a uuid
//  private val _pcDetails = mutable.Map.empty[String, PcDetails]
  private var _pcDetails: mutable.Map[String, PcDetails] = null

  val _publisherRegion = ClusterSharding(context.system).shardRegion(Publisher.shardName)

  var _pendingSubscriptions: mutable.Map[String, (ActorRef, Set[String])] = null

  _init()

  def _init(): Unit = {
    _pcDetails = mutable.Map.empty[String, PcDetails]
    _pendingSubscriptions = mutable.Map.empty[String, (ActorRef, Set[String])]
  }

  override def receive: Receive = {
    case Internal.Registry.AddStream(identifier, mediaStream) =>
      val duplicatedMediaStream = webRtcHelper.createDuplicateMediaStream(mediaStream, identifier)
      _pcDetails.get(identifier).get.addStream(duplicatedMediaStream)
    case Internal.Registry.Initialize(identifier) =>
      _init()
    case Internal.Offer(identifier, uuid, offer) =>
      val logId = Log.registrySubPcId(identifier, uuid)
      log.info(s"$logId offer received.")
      _ensurePeerConnection(identifier, uuid)
      val answer = webRtcHelper.createAnswer(_pcDetails.get(identifier).get.peerConnection, offer)
      if (answer.isDefined) {
        sender ! Internal.Answer(identifier, uuid, answer.get)
      } else {
        log.error("Failed to create answer! No answer will be sent back for offer!")
      }
    case Internal.Candidate(identifier, uuid, candidate) =>
      val logId = Log.registrySubPcId(identifier, uuid)
      log.info(s"$logId candidate received.")
      _ensurePeerConnection(identifier, uuid)
      _pcDetails.get(identifier).get.peerConnection.addIceCandidate(candidate)

    case Incoming.Subscribe(identifier: String, publisherIdentifier: String) =>
      log.info(s"($identifier) subscribing to $publisherIdentifier.")
      if(_pcDetails.get(publisherIdentifier).nonEmpty) {
        _subscribe(identifier, publisherIdentifier, sender())
      } else { // Does not exist yet! We need to start attempting initialization
        val set = _pendingSubscriptions.getOrElse(publisherIdentifier, (sender(), Set.empty[String]))
        _pendingSubscriptions.put(publisherIdentifier, (sender(), set._2 + identifier))
        _publisherRegion ! Internal.RequestPeerConnection(publisherIdentifier)
      }
      // TODO: Add the ability to subscribe to a single label
    case Internal.Registry.ProcessPendingSubscriptions(identifier) =>
      if(_pendingSubscriptions.get(identifier).isDefined) {
        _pendingSubscriptions.foreach {
          case (publisherIdentifier, refs) =>
            val actorRef = refs._1
            refs._2.foreach {
              case (subscriberIdentifier) =>
                _subscribe(subscriberIdentifier, publisherIdentifier, actorRef)
            }
        }
      }
  }
  
  def _subscribe(subscriberIdentifier: String, publisherIdentifier: String, actorRef: ActorRef): Unit = {
    _pcDetails.get(publisherIdentifier).get.getMediaStreams.foreach {
      case (mediaStreamId, mediaStream) => {
        log.info(s"Attempting to add $mediaStreamId to $subscriberIdentifier (${mediaStream.label()})...")
        callback.onSubscribe(subscriberIdentifier, publisherIdentifier, mediaStream.label())
        actorRef ! Internal.AddRegistryMediaStream(subscriberIdentifier, mediaStreamId, mediaStream)
      }
    }
  }

  def _ensurePeerConnection(identifier: String, uuid: String): Unit = {
    if (!_pcDetails.contains(identifier)) {
      val logId = Log.registrySubPcId(identifier, uuid)
      val observer = new RegistryToPublisherPcObs(
        log,
        logId,
        webRtcHelper,
        self,
        sender(),
        callback,
        identifier,
        uuid)

      log.info(s"Creating $logId.")
      callback.onRegistrySubInitialize(identifier, uuid, self.path.toString)
      _pcDetails.put(identifier, new StandardPcDetails(webRtcHelper.createPeerConnection(observer), webRtcHelper))
    }
  }
}
