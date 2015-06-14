package server.app

import akka.actor._
import akka.contrib.pattern.ClusterSharding
import server.registry.Registry
import server.{Publisher, Subscriber}
import tv.camfire.media.config.ClusterModule

object SubscriberApp {

  def run(p: Int): Unit = {
    val modules: ClusterModule = new ClusterModule {
      def port(): String = {
        p.toString
      }
    }
    val system = modules.actorSystem
    val properties = modules.properties

    ClusterSharding(system).start(
      typeName = Publisher.shardName,
      entryProps = None,
      idExtractor = Publisher.idExtractor,
      shardResolver = Publisher.shardResolver)

    ClusterSharding(system).start(
      typeName = Subscriber.shardName,
      entryProps = Some(Subscriber.props(modules.webRtcHelper, modules.subscriberCallback)),
      idExtractor = Subscriber.idExtractor,
      shardResolver = Subscriber.shardResolver)

    system.actorOf(Props(new Registry(modules.webRtcHelper, modules.subscriberCallback)), "registry")
  }
}

