package server

import akka.actor._
import akka.contrib.pattern.ClusterSharding
import akka.pattern.ask
import akka.persistence.journal.leveldb.{SharedLeveldbJournal, SharedLeveldbStore}
import akka.util.Timeout
import server.registry.Registry
import tv.camfire.media.config.LogicModule
import tv.camfire.redis.{RedisSubscriberSignalMonitor, RedisPublisherSignalMonitor}

import scala.concurrent.duration._

object App {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty)
        startup(Seq("2551", "2552", "2553", "2554"))
    else
      startup(args)
  }

  def sharedJournalPort = "2551"
  var ref: ActorRef = null

  def startup(ports: Seq[String]): Unit = {
    ports foreach { _port =>
      val modules: LogicModule = new LogicModule {
        def port(): String = {
          _port
        }
      }

      val system = modules.actorSystem

      startupSharedJournal(system, startStore = _port == sharedJournalPort, path =
        ActorPath.fromString(s"akka.tcp://ClusterSystem@127.0.0.1:$sharedJournalPort/user/store"))

      _port match {
        case "2551" =>
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
        case "2552" =>
          ClusterSharding(system).start(
            typeName = Publisher.shardName,
            entryProps = None, // Starting in Proxy mode
            idExtractor = Publisher.idExtractor,
            shardResolver = Publisher.shardResolver)

          val channels = Seq()
          val patterns = Seq("media.publisher.*")
          system.actorOf(Props(classOf[RedisPublisherSignalMonitor], channels, patterns)
            .withDispatcher("rediscala.rediscala-client-worker-dispatcher"))
        case "2553" =>
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
        case "2554" =>
          val subscriberRegion = ClusterSharding(system).start(
            typeName = Subscriber.shardName,
            entryProps = None, // Starting in Proxy mode
            idExtractor = Subscriber.idExtractor,
            shardResolver = Subscriber.shardResolver)

          val channels = Seq()
          val patterns = Seq("media.subscriber.*")
          system.actorOf(Props(classOf[RedisSubscriberSignalMonitor], channels, patterns)
            .withDispatcher("rediscala.rediscala-client-worker-dispatcher"))

        case _ =>
        //          val authorListingRegion = ClusterSharding(system).start(
        //            typeName = Subscriber.shardName,
        //            entryProps = None,
        //            idExtractor = Subscriber.idExtractor,
        //            shardResolver = Subscriber.shardResolver)
        //          ClusterSharding(system).start(
        //            typeName = Publisher.shardName,
        //            entryProps = None,
        //            idExtractor = Publisher.idExtractor,
        //            shardResolver = Publisher.shardResolver)
        //          if (port != "2551" && port != "2552")
        //            system.actorOf(Props[Bot], "bot")
      }
    }


    def startupSharedJournal(system: ActorSystem, startStore: Boolean, path: ActorPath, name: String="store"): Unit = {
      // Start the shared journal one one node (don't crash this SPOF)
      // This will not be needed with a distributed journal
      if (startStore)
        ref = system.actorOf(Props[SharedLeveldbStore], name)
      // register the shared journal
//      import system.dispatcher
//      implicit val timeout = Timeout(25.seconds)
//      val f = (system.actorSelection(path) ? Identify(None))
//      f.onSuccess {
//        case ActorIdentity(_, Some(ref)) => SharedLeveldbJournal.setStore(ref, system)
//        case _ =>
//          system.log.error("Shared journal not started at {}", path)
//          system.shutdown()
//      }
//      f.onFailure {
//        case _ =>
//          system.log.error("Lookup of shared journal at {} timed out", path)
//          system.shutdown()
//      }

        SharedLeveldbJournal.setStore(ref, system)


//      case ActorIdentity(_, Some(actorRef)) =>
//      ref = actorRef
//      context watch ref
//      case ActorIdentity(_, None) => // not alive
    }

  }

}

