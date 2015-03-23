package server

import akka.actor._
import akka.contrib.pattern.ClusterSharding
import akka.persistence.journal.leveldb.{SharedLeveldbJournal, SharedLeveldbStore}
import tv.camfire.media.config.LogicModule
import tv.camfire.redis.{RedisPublisherSignalMonitor, RedisSubscriberSignalMonitor}

case class Config(mode: String = "", port: Int = -1, startStore: Boolean = false, kwargs: Map[String, String] = Map())

object App {
  def isAllDigits(x: String) = x forall Character.isDigit

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
    // parser.parse returns Option[C]
    parser.parse(args, Config()) match {
      case Some(config) =>
        val p = config.port
        val modules: LogicModule = new LogicModule {
          def port(): String = {
            p.toString
          }
        } // do stuff
      val system = modules.actorSystem

        startupSharedJournal(system, startStore = config.startStore, path =
          ActorPath.fromString(s"akka.tcp://ClusterSystem@127.0.0.1:$sharedJournalPort/user/store"))

        if (config.mode == "publisher") {
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
          ClusterSharding(system).start(
            typeName = Publisher.shardName,
            entryProps = None, // Starting in Proxy mode
            idExtractor = Publisher.idExtractor,
            shardResolver = Publisher.shardResolver)

          val channels = Seq()
          val patterns = Seq("media.publisher.*")
          system.actorOf(Props(classOf[RedisPublisherSignalMonitor], channels, patterns)
            .withDispatcher("rediscala.rediscala-client-worker-dispatcher"))
        } else if (config.mode == "subscriber") {
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

        } else if (config.mode == "subscriber-monitor") {
          val subscriberRegion = ClusterSharding(system).start(
            typeName = Subscriber.shardName,
            entryProps = None, // Starting in Proxy mode
            idExtractor = Subscriber.idExtractor,
            shardResolver = Subscriber.shardResolver)

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

  def startupSharedJournal(system: ActorSystem, startStore: Boolean, path: ActorPath, name: String = "store"): Unit = {
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


  var ref: ActorRef = null
}

