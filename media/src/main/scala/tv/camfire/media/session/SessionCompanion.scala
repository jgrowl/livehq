//package tv.camfire.media.session
//
//import akka.actor.{ActorRef, ActorLogging, Actor}
//import tv.camfire.media.webrtc.WebRtcHelper
//import org.webrtc.{PeerConnection, MediaStream}
//import tv.camfire.media.factory.CamfirePeerConnectionFactory
//import tv.camfire.common._
//import tv.camfire.media.callback.Callback
//
//
///**
// * User: jonathan
// * Date: 5/2/13
// * Time: 9:05 PM
// */
//
//class SessionCompanion(webRtcHelper: WebRtcHelper,
//                       camfirePeerConnectionFactory: CamfirePeerConnectionFactory,
//                       _sessionId: String, callback: Callback,
//                       sendWrapper: ActorRef) extends Actor with ActorLogging {
//  protected implicit var _resourceUuid: String = null
//
//  private val initializationTime = System.currentTimeMillis
//  log.debug("Created session companion [%s] at [%s]".format(_sessionId, initializationTime))
//
//  implicit val actorSystem = context.system
//
//  log.debug("Creating PeerConnection for [{}]", _sessionId)
////  private var peerConnection = webRtcHelper.createPeerConnection(_sessionId, callback, sendWrapper)
//  private var peerConnection : PeerConnection = null
//  private var _registeredMediaStream: MediaStream = null
//
//  def receive = {
//    case Incoming.Offer(sessionId, remoteSessionDescription) =>
//      // TODO: Validate this description
//      log.debug("Establishing peerConnection for [{}]", _sessionId)
//      webRtcHelper.establishPeerConnection(peerConnection, remoteSessionDescription)
//      sendWrapper ! Outgoing.Answer(peerConnection.getLocalDescription)
//    case Incoming.Answer(sessionId, remoteSessionDescription) =>
//      log.debug("Setting PeerConnection's remote description received in answer for [%s]".format(sessionId))
//      webRtcHelper.setRemoteDescription(peerConnection, remoteSessionDescription)
//    case Incoming.IceCandidate(sessionId, iceCandidate) =>
//      log.debug("Adding iceCandidate to [%s] session...".format(sessionId))
//      peerConnection.addIceCandidate(iceCandidate)
//    case Internal.AddStream(sessionId, mediaStream) =>
//      _registeredMediaStream = camfirePeerConnectionFactory.createDuplicatedMediaStream(mediaStream, sessionId)
//      val mediaStreamLabel = mediaStream.label()
//      log.debug("Adding media stream {} to {} session...", mediaStreamLabel, sessionId)
////      callback.onAddStream(_sessionId, mediaStreamLabel)
//    case Internal.SubscribeNotification(requester, label) =>
//      requester ! Internal.Subscribe(_registeredMediaStream)
//    case Internal.Subscribe(mediaStream: MediaStream) =>
//      peerConnection.addStream(mediaStream, webRtcHelper.createConstraints)
//      webRtcHelper.makeOffer(peerConnection)
////      sendWrapper ! outgoing.Offer(peerConnection.getLocalDescription)
////      sender ! Outgoing.Answer(peerConnection.getLocalDescription)
//    case msg@_ =>
//      log.error("Received an unknown message!")
//      println(msg)
//  }
//}
