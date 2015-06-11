package server.app

import akka.actor._
import akka.contrib.pattern.ClusterSharding
import server.{Publisher, Subscriber}
import tv.camfire.media.config.ClusterModule
import tv.camfire.redis.RedisSubscriberSignalMonitor

object SubscriberMonitorApp {

  def run(p: Int, startStore: Boolean): Unit = {
    val modules: ClusterModule = new ClusterModule {
      def port(): String = {
        p.toString
      }
    }
    val system = modules.actorSystem
    val properties = modules.properties

    ClusterSharding(system).start(
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
    system.actorOf(Props(classOf[RedisSubscriberSignalMonitor], channels, patterns, properties)
      .withDispatcher("rediscala.rediscala-client-worker-dispatcher"))


  }
}

