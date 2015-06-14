package server.app

import akka.actor._
import akka.contrib.pattern.ClusterSharding
import server.{Publisher, Subscriber}
import tv.camfire.media.config.ClusterModule

object PublisherApp {

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
      entryProps = Some(Publisher.props(modules.webRtcHelper, modules.publisherCallback)),
      idExtractor = Publisher.idExtractor,
      shardResolver = Publisher.shardResolver
    )

    ClusterSharding(system).start(
      typeName = Subscriber.shardName,
      entryProps = None, // Starting in Proxy mode
      idExtractor = Subscriber.idExtractor,
      shardResolver = Subscriber.shardResolver)
  }

}

