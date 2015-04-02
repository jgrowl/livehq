package server

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._

class ClusterMonitor extends Actor with ActorLogging {
  def receive = {
    case state: CurrentClusterState           => log.info("Current state: {}", state)
    case MemberUp(member)                     => log.info("Member is up: {}, roles: {}", member, member.roles)
    case MemberRemoved(member, previousState) => log.info("Member removed: {}, roles: {}", member, member.roles)
    case MemberExited(member)                 => log.info("Member exited: {}, roles: {}", member, member.roles)
    case UnreachableMember(member)            => log.info("Member unreachable: {}, roles: {}", member, member.roles)
    case LeaderChanged(address)               => log.info("Leader changed: {}", address)
    case RoleLeaderChanged(role, member)      => log.info("Role {} leader changed: {}", role, member)
    case _: ClusterMetricsChanged             => // ignore
    case e: ClusterDomainEvent                => //log.info("???: {}", e)
  }
}

object ClusterMonitor {
//
//  def main(args: Array[String]) {
//    if (args.isEmpty) {
//      startup(Seq("2551"))
//    } else {
//      startup(args)
//    }
//
//  }

  def startup(ports: Seq[String]): Unit = {
//    ports foreach { _port =>
//      val modules = new SubscriberModule {
//        def port(): String = {
//          _port
//        }
//      }
//
//      val system = modules.actorSystem
//
////      startupSharedJournal(system, startStore = true, path =
////        ActorPath.fromString("akka.tcp://ClusterSystem@127.0.0.1:2551/user/store"))
//
//      val cluster = Cluster(system)
//      val monitor = system.actorOf(Props[ClusterMonitor])
//      cluster.subscribe(monitor, classOf[ClusterDomainEvent])
//    }
  }

//  def startupSharedJournal(system: ActorSystem, startStore: Boolean, path: ActorPath): Unit = {
//    // Start the shared journal one one node (don't crash this SPOF)
//    // This will not be needed with a distributed journal
//    system.log.info("Starting shared journal...")
//    if (startStore)
//      system.actorOf(Props[SharedLeveldbStore], "store")
//    // register the shared journal
//    import system.dispatcher
//    implicit val timeout = Timeout(15.seconds)
//    val f = (system.actorSelection(path) ? Identify(None))
//    f.onSuccess {
//      case ActorIdentity(_, Some(ref)) => SharedLeveldbJournal.setStore(ref, system)
//      case _ =>
//        system.log.error("Shared journal not started at {}", path)
//        system.shutdown()
//    }
//    f.onFailure {
//      case _ =>
//        system.log.error("Lookup of shared journal at {} timed out", path)
//        system.shutdown()
//    }
//  }


}