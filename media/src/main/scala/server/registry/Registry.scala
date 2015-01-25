package server.registry

import akka.actor._
import tv.camfire.media.callback.Callback
import tv.camfire.media.webrtc.WebRtcHelper

import scala.collection.mutable


object Registry {
  object Incoming {
    sealed trait Incoming
    case class Subscribe(identifier: String) extends Incoming
  }
}

/**
 * User: jonathan
 * Date: 5/2/13
 * Time: 8:06 PM
 */
class Registry(webRtcHelper: WebRtcHelper, callback: Callback) extends Actor with ActorLogging {
  private val _entries = mutable.Map.empty[String, ActorRef]

  private def _getOrCreateEntry(identifier: String, sender: ActorRef) = _entries getOrElse(identifier, {
    val c = context actorOf Props(new RegistryEntry(webRtcHelper, callback, identifier, sender))
    _entries += identifier -> c
    context watch c
    c
  })

  override def receive: Receive = {
    case command: Registry.Incoming.Subscribe =>
      _getOrCreateEntry(command.identifier, sender()).tell(command, sender())
    case Terminated(ref) =>
      log.info("Removing entry...")
      _entries.remove(_entries.map(_.swap).get(ref).get)
  }
}
