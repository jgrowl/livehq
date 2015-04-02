package server

import akka.actor._
import akka.contrib.pattern.ClusterSharding
import tv.camfire.media.config.LogicModule

object PublisherApp {

  def sharedJournalPort = "2551"

  def run(p: Int, startStore: Boolean): Unit = {
    val modules: LogicModule = new LogicModule {
      def port(): String = {
        p.toString
      }
    }
    val system = modules.actorSystem

    SharedJournalStarter.startupSharedJournal(system, startStore = startStore, path =
      ActorPath.fromString(s"akka.tcp://ClusterSystem@livehq-publisher-seed:$sharedJournalPort/user/store"))

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

