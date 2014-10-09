//package tv.camfire.media.config.factory
//
//import tv.camfire.media.factory.CamfirePeerConnectionFactory
//import tv.camfire.media.webrtc.WebRtcHelper
//import akka.actor.{Props, ActorRef, ActorContext}
//import tv.camfire.media.session.SessionCompanion
//import tv.camfire.media.callback.Callback
//
///**
// * User: jonathan
// * Date: 7/23/13
// * Time: 3:10 AM
// */
//class SessionCompanionFactoryFactory(camfirePeerConnectionFactory: CamfirePeerConnectionFactory,
//                                     webRtcHelper: WebRtcHelper,
//                                     callback: Callback) {
//
//  case class create(implicit context: ActorContext) {
//    def createSessionCompanion(sessionId: String, sender: ActorRef): ActorRef = {
//      val props = Props(
//        new SessionCompanion(
//          webRtcHelper,
//          camfirePeerConnectionFactory,
//          sessionId, callback, sender))
//      context.actorOf(props, sessionId)
//    }
//  }
//
//}
