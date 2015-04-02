package server

import akka.actor._
import akka.contrib.pattern.ClusterSharding
import tv.camfire.media.config.LogicModule
import tv.camfire.redis.{RedisSubscriberSignalMonitor, RedisPublisherSignalMonitor}

object SubscriberMonitorApp {

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

    val subscriberRegion = ClusterSharding(system).start(
      typeName = Subscriber.shardName,
      entryProps = None, // Starting in Proxy mode
      idExtractor = Subscriber.idExtractor,
      shardResolver = Subscriber.shardResolver)

    ClusterSharding(system).start(
      typeName = Publisher.shardName,
      entryProps = None, // Starting in Proxy mode
      idExtractor = Publisher.idExtractor,
      shardResolver = Publisher.shardResolver)

    val channels = Seq()
    val patterns = Seq("media.subscriber.*")
    system.actorOf(Props(classOf[RedisSubscriberSignalMonitor], channels, patterns)
      .withDispatcher("rediscala.rediscala-client-worker-dispatcher"))


  }
}

