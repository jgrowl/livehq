package tv.camfire.actor

import akka.actor.Actor
import akka.event.LoggingAdapter

/**
 * User: jonathan
 * Date: 5/2/13
 * Time: 8:06 PM
 */
trait UnknownMessageHandler extends Actor {
  val log: LoggingAdapter

  protected def unknownMessageHandler: Receive = {
    case _ =>
      log.warning("Unknown message")
  }
}
