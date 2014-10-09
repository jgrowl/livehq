package server

import akka.actor.{ActorIdentity, ActorPath, ActorSystem, Identify, Props}
import akka.contrib.pattern.ClusterSharding
import akka.pattern.ask
import akka.persistence.journal.leveldb.{SharedLeveldbJournal, SharedLeveldbStore}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import tv.camfire.media.config.LogicModule

import scala.concurrent.duration._

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

      startupSharedJournal(system, startStore = (_port == "2551"), path =
        ActorPath.fromString("akka.tcp://ClusterSystem@127.0.0.1:2551/user/store"))

      val connectionRegion = ClusterSharding(system).start(
        typeName = Publisher.shardName,
        entryProps = Some(Publisher.props(modules.webRtcHelper, modules.callback)),
        idExtractor = Publisher.idExtractor,
        shardResolver = Publisher.shardResolver)

//      ClusterSharding(system).start(
//        typeName = Post.shardName,
//        entryProps = Some(Post.props(authorListingRegion)),
//        idExtractor = Post.idExtractor,
//        shardResolver = Post.shardResolver)


//      if (port != "2551" && port != "2552") {
//        system.actorOf(Props[Bot], "bot")
//      }

    }

    def startupSharedJournal(system: ActorSystem, startStore: Boolean, path: ActorPath): Unit = {
      // Start the shared journal one one node (don't crash this SPOF)
      // This will not be needed with a distributed journal
      if (startStore)
        system.actorOf(Props[SharedLeveldbStore], "store")
      // register the shared journal
      import system.dispatcher
      implicit val timeout = Timeout(15.seconds)
      val f = (system.actorSelection(path) ? Identify(None))
      f.onSuccess {
        case ActorIdentity(_, Some(ref)) => SharedLeveldbJournal.setStore(ref, system)
        case _ =>
          system.log.error("Shared journal not started at {}", path)
          system.shutdown()
      }
      f.onFailure {
        case _ =>
          system.log.error("Lookup of shared journal at {} timed out", path)
          system.shutdown()
      }
    }
  }

}

