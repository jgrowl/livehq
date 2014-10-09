package server

import akka.actor.{ActorSystem, Props}
import akka.contrib.pattern.ClusterSharding
import com.typesafe.config.ConfigFactory
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

      val connectionRegion = ClusterSharding(system).start(
        typeName = Publisher.shardName,
        entryProps = Some(Publisher.props(modules.webRtcHelper, modules.callback)),
        idExtractor = Publisher.idExtractor,
        shardResolver = Publisher.shardResolver)

      val channels = Seq()
      val patterns = Seq("media.*")
      system.actorOf(Props(classOf[SubscribeActor], channels, patterns).withDispatcher("rediscala.rediscala-client-worker-dispatcher"))

    }
  }
}

