package tv.camfire.media.config.factory

import akka.actor.{ActorSystem, ActorRef}
import tv.camfire.media.webrtc.PeerConnectionObserver
import tv.camfire.media.callback.Callback

/**
 * User: jonathan
 * Date: 7/23/13
 * Time: 3:10 AM
 */
class PeerConnectionObserverFactoryFactory(callback: Callback) {

  case class create() {
    def create(identifier: String): PeerConnectionObserver = {
      new PeerConnectionObserver(identifier, callback)
    }
  }

}
