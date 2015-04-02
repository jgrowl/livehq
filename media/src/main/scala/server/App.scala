package server

import akka.actor._
import akka.contrib.pattern.ClusterSharding
import akka.persistence.journal.leveldb.{SharedLeveldbJournal, SharedLeveldbStore}
import server.registry.Registry
import tv.camfire.media.config.LogicModule
import tv.camfire.redis.{RedisPublisherSignalMonitor, RedisSubscriberSignalMonitor}
import akka.util.Timeout
import akka.pattern.ask
import akka.actor.Identify
import akka.actor.ActorIdentity
import scala.concurrent.duration._

case class Config(mode: String = "", port: Int = -1, startStore: Boolean = false, kwargs: Map[String, String] = Map())

object App {
  def main(args: Array[String]): Unit = {
    val parser = new scopt.OptionParser[Config]("livehq-media") {
      head("livehq-media", "0.x")
      cmd("publisher") action { (_, c) =>
        c.copy(mode = "publisher")
      } text "publisher" children(
        opt[Int]("port") abbr "p" action { (x, c) =>
          c.copy(port = x)
        } text "sets port",
        opt[Boolean]("startStore") abbr "s" action { (x, c) =>
          c.copy(startStore = x)
        } text "starts store",
        checkConfig { c =>
          if (c.port < 0) failure("port must be greater than zero") else success
        }
        )
      cmd("publisher-monitor") action { (_, c) =>
        c.copy(mode = "publisher-monitor")
      } text "publisher-monitor" children(
        opt[Int]("port") abbr "p" action { (x, c) =>
          c.copy(port = x)
        } text "sets port",
        checkConfig { c =>
          if (c.port < 0) failure("port must be greater than zero") else success
        }
        )

      cmd("subscriber") action { (_, c) =>
        c.copy(mode = "subscriber")
      } text "subscriber" children(
        opt[Int]("port") abbr "p" action { (x, c) =>
          c.copy(port = x)
        } text "sets port",
        checkConfig { c =>
          if (c.port < 0) failure("port must be greater than zero") else success
        }
        )
      cmd("subscriber-monitor") action { (_, c) =>
        c.copy(mode = "subscriber-monitor")
      } text "subscriber-monitor" children(
        opt[Int]("port") abbr "p" action { (x, c) =>
          c.copy(port = x)
        } text "sets port",
        checkConfig { c =>
          if (c.port < 0) failure("port must be greater than zero") else success
        }
        )
    }
    parser.parse(args, Config()) match {
      case Some(config) =>
        if (config.mode == "publisher") {

          val p = config.port
          val modules: LogicModule = new LogicModule {
            def port(): String = {
              p.toString
            }
          } // do stuff
          val system = modules.actorSystem

          startupSharedJournal(system, startStore = config.startStore, path =
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
        } else if (config.mode == "publisher-monitor") {
          val p = config.port
          val modules: LogicModule = new LogicModule {
            def port(): String = {
              p.toString
            }
          } // do stuff
          val system = modules.actorSystem

          startupSharedJournal(system, startStore = config.startStore, path =
            ActorPath.fromString(s"akka.tcp://ClusterSystem@livehq-publisher-seed:$sharedJournalPort/user/store"))

          ClusterSharding(system).start(
            typeName = Publisher.shardName,
            entryProps = None, // Starting in Proxy mode
            idExtractor = Publisher.idExtractor,
            shardResolver = Publisher.shardResolver)

          val subscriberRegion = ClusterSharding(system).start(
            typeName = Subscriber.shardName,
            entryProps = None, // Starting in Proxy mode
            idExtractor = Subscriber.idExtractor,
            shardResolver = Subscriber.shardResolver)

          val channels = Seq()
          val patterns = Seq("media.publisher.*")
          system.actorOf(Props(classOf[RedisPublisherSignalMonitor], channels, patterns)
            .withDispatcher("rediscala.rediscala-client-worker-dispatcher"))
        } else if (config.mode == "subscriber") {

          val p = config.port
          val modules: LogicModule = new LogicModule {
            def port(): String = {
              p.toString
            }
          } // do stuff
          val system = modules.actorSystem

          startupSharedJournal(system, startStore = config.startStore, path =
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
        } else if (config.mode == "subscriber-monitor") {

          val p = config.port
          val modules: LogicModule = new LogicModule {
            def port(): String = {
              p.toString
            }
          } // do stuff
          val system = modules.actorSystem

          startupSharedJournal(system, startStore = config.startStore, path =
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

      case None =>
      // arguments are bad, error message will have been displayed
    }
  }

  def sharedJournalPort = "2551"

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

