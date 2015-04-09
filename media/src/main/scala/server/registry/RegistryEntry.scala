package server.registry

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.contrib.pattern.ClusterSharding
import livehq._
import server.Publisher.RequestPeerConnection
import server.pc.obs.RegistryToPublisherPcObs
import server.{Publisher, Utils}
import tv.camfire.media.callback.SubscriberCallback
import tv.camfire.media.webrtc.WebRtcHelper

import scala.collection.mutable


/**
 * Created by jonathan on 12/12/14.
 */
class RegistryEntry(webRtcHelper: WebRtcHelper, callback: SubscriberCallback, _identifier: String)
  extends Actor with ActorLogging {

  private val _publisherRegion = ClusterSharding(context.system).shardRegion(Publisher.shardName)

  private val _uuid = Utils.uuid

  private val _pcDetails: PcDetails = _initPcDetails()

  _publisherRegion ! RequestPeerConnection(_identifier, _uuid)

  private val _pendingSubscribers = mutable.Set.empty[(String, ActorRef)]
  private var _connected = false

  override def receive: Receive = {
    case Registry.Incoming.Subscribe(identifier: String) =>
      log.info(s"($identifier) subscribing to ${_identifier}.")
      _addSubscriber(identifier, sender())

    case Internal.Offer(identifier, uuid, offer) =>
      val logId = Log.registrySubPcId(identifier, uuid)
      log.info(s"$logId offer received.")
      val answer = webRtcHelper.createAnswer(_pcDetails.peerConnection, offer)
      if (answer.isDefined) {
        sender ! Internal.Answer(identifier, uuid, answer.get)
      } else {
        log.error("Failed to create answer! No answer will be sent back for offer!")
      }
    case Internal.Candidate(identifier, uuid, candidate) =>
      val logId = Log.registrySubPcId(identifier, uuid)
      log.info(s"$logId candidate received.")
      _pcDetails.peerConnection.addIceCandidate(candidate)
    case Internal.Registry.AddStream(identifier, mediaStream) =>
      log.info(s"Adding stream ${mediaStream.label()}.")
      _pcDetails.addStream(mediaStream)
    // TODO: Add the ability to subscribe to a single label
    case Internal.Registry.Connected(identifier) =>
      _connected = true
      _processPendingSubscribers()
  }

  private def _initPcDetails(): PcDetails = {
    val logId = Log.registrySubPcId(_identifier, _uuid)
    val observer = new RegistryToPublisherPcObs(
      log,
      logId,
      webRtcHelper,
      self,
      callback,
      _identifier,
      _uuid)

    log.info(s"Creating $logId.")
    callback.onRegistrySubInitialize(_identifier, _uuid, self.path.toString)
    new StandardPcDetails(webRtcHelper.createPeerConnection(observer), webRtcHelper)
  }

  private def _addSubscriber(identifier: String, sender: ActorRef): Unit = {
    if (_connected) {
      _subscribe(identifier, sender)
    } else {
      _pendingSubscribers += identifier -> sender
    }
  }

  private def _subscribe(identifier: String, subscriber: ActorRef): Unit = {
    _pcDetails.getMediaStreams.foreach {
      case (mediaStreamId, mediaStream) => {
        log.info(s"Attempting to add $mediaStreamId to $identifier (${mediaStream.label()})...")
        callback.onSubscribe(identifier, _identifier, mediaStream.label())
        subscriber ! Internal.AddRegistryMediaStream(mediaStreamId, mediaStream)
      }
    }
  }

  private def _processPendingSubscribers(): Unit = {
    _pendingSubscribers.foreach {
      case (identifier, subscriber) =>
        _subscribe(identifier, subscriber)
    }
  }
}
