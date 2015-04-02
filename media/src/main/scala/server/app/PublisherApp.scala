package server.app

import akka.actor._
import akka.contrib.pattern.ClusterSharding
import server.{Publisher, SharedJournalStarter, Subscriber}
import tv.camfire.media.config.LogicModule

object PublisherApp {

  def run(p: Int, startStore: Boolean): Unit = {
    val modules: LogicModule = new LogicModule {
      def port(): String = {
        p.toString
      }
    }
    val system = modules.actorSystem
    val properties = modules.properties

    SharedJournalStarter.startupSharedJournal(system, startStore = startStore, path =
      ActorPath.fromString(properties.sharedJournalPath))

    ClusterSharding(system).start(
      typeName = Publisher.shardName,
      entryProps = Some(Publisher.props(modules.webRtcHelper, modules.callback)),
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

