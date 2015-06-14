package tv.camfire.media.config

import akka.actor.ActorSystem
import com.softwaremill.macwire.Macwire
import com.typesafe.config.ConfigFactory

/**
 * User: jonathan
 * Date: 7/22/13
 * Time: 7:05 PM
 */
trait BaseModule extends Macwire {
  /**
   * Utilities & Configuration
   */
  lazy val properties: Properties = new Properties {}

  def port(): String

  // Override the configuration of the port
  val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
    withFallback(ConfigFactory.load())

  lazy implicit val actorSystem = ActorSystem(properties.actorSystemName, config)
}
