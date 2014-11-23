package server

import akka.actor.Props
import akka.contrib.pattern.ClusterSharding
import tv.camfire.actor.Registry
import tv.camfire.media.config.LogicModule

object WebRtcApp {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty)
      startup(Seq("2551", "2552", "0"))
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

      val registry = system.actorOf(Props(new Registry(modules.webRtcHelper, modules.callback)), "registry")

      val connectionRegion = ClusterSharding(system).start(
        typeName = Publisher.shardName,
        entryProps = Some(Publisher.props(modules.webRtcHelper, modules.callback)),
        idExtractor = Publisher.idExtractor,
        shardResolver = Publisher.shardResolver)

//      if (port != "2551" && port != "2552") {
//        system.actorOf(Props[Bot], "bot")
//      }
    }
  }
}

