package tv.camfire.media

import tv.camfire.media.config.factory.{PeerConnectionObserverFactoryFactory, SessionCompanionFactoryFactory}


/**
 * User: jonathan
 * Date: 7/22/13
 * Time: 11:26 PM
 */
object Types {
  type SessionCompanionFactory = SessionCompanionFactoryFactory#create
  type PeerConnectionObserverFactory = PeerConnectionObserverFactoryFactory#create
}
