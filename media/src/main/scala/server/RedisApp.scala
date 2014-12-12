package server

import akka.actor.Props
import akka.contrib.pattern.ClusterSharding
import tv.camfire.media.config.LogicModule
import tv.camfire.redis.SubscribeActor

object RedisApp {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty)
      startup(Seq("2553"))
    else
      startup(args)
  }

  def startup(ports: Seq[String]): Unit = {
    ports foreach { _port =>

      val modules: LogicModule = new LogicModule {
        def port(): String = {
          _port
        }
      }

      val system = modules.actorSystem

      ClusterSharding(system).start(
        typeName = Publisher.shardName,
        entryProps = None, // Starting in Proxy mode
        idExtractor = Publisher.idExtractor,
        shardResolver = Publisher.shardResolver)

      val channels = Seq()
      val patterns = Seq("media.*")
      system.actorOf(Props(classOf[SubscribeActor], channels, patterns)
        .withDispatcher("rediscala.rediscala-client-worker-dispatcher"))
    }
  }
}

