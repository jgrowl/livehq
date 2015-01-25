package blog

import java.util.UUID

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.contrib.pattern.ClusterSharding
import server.{Subscriber, Publisher}

import scala.concurrent.duration._

object Bot {
  private case object Tick
}

class Bot extends Actor with ActorLogging {
  import blog.Bot._
  import context.dispatcher
  val tickTask = context.system.scheduler.schedule(3.seconds, 3.seconds, self, Tick)

  val postRegion = ClusterSharding(context.system).shardRegion(Publisher.shardName)
  val listingsRegion = ClusterSharding(context.system).shardRegion(Subscriber.shardName)

  val from = Cluster(context.system).selfAddress.hostPort

  override def postStop(): Unit = {
    super.postStop()
    tickTask.cancel()
  }

  var n = 0
  val authors = Map(0 -> "Patrik", 1 -> "Martin", 2 -> "Roland", 3 -> "Björn", 4 -> "Endre")
  def currentAuthor = authors(n % authors.size)

  def receive = create

  val create: Receive = {
    case Tick =>
      val postId = UUID.randomUUID().toString
      n += 1
      val title = s"Post $n from $from"
//      postRegion ! Publisher.AddPost(postId, Publisher.PostContent(currentAuthor, title, "..."))
      context.become(edit(postId))
  }

  def edit(postId: String): Receive = {
    case Tick =>
//      postRegion ! Publisher.ChangeBody(postId, "Something very interesting ...")
      context.become(publish(postId))
  }

  def publish(postId: String): Receive = {
    case Tick =>
//      postRegion ! Publisher.Publish(postId)
      context.become(list)
  }

  val list: Receive = {
    case Tick =>
//      listingsRegion ! Subscriber.GetPosts(currentAuthor)
//    case Subscriber.Posts(summaries) =>
//      log.info("Posts by {}: {}", currentAuthor, summaries.map(_.title).mkString("\n\t", "\n\t", ""))
//      context.become(create)
  }

}
