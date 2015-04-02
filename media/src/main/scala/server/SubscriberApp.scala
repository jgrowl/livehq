package server

import akka.actor._
import akka.contrib.pattern.ClusterSharding
import server.registry.Registry
import tv.camfire.media.config.LogicModule
import tv.camfire.redis.RedisPublisherSignalMonitor

object SubscriberApp {

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

    val publisherRegion = ClusterSharding(system).start(
      typeName = Publisher.shardName,
      entryProps = None,
      idExtractor = Publisher.idExtractor,
      shardResolver = Publisher.shardResolver)

    val subscriberRegion = ClusterSharding(system).start(
      typeName = Subscriber.shardName,
      entryProps = Some(Subscriber.props(modules.webRtcHelper, modules.subscriberCallback)),
      idExtractor = Subscriber.idExtractor,
      shardResolver = Subscriber.shardResolver)

    system.actorOf(Props(new Registry(modules.webRtcHelper, modules.callback)), "registry")
  }
}

