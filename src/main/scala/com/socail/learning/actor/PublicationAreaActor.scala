package com.socail.learning.actor

import akka.actor.{ Actor, ActorRef }
import com.socail.learning.domain.Publication

import scala.collection.mutable

trait PublicationEvent

case class UserJoined(id: Int, actorRef: ActorRef) extends PublicationEvent

case class UserLeft(id: Int) extends PublicationEvent

case class PublicationAdded(publication: Publication) extends PublicationEvent

case class MessageRecived(message: String) extends PublicationEvent

case class IdWithActor(id: Int, actor: ActorRef)

class PublicationAreaActor extends Actor {

  val users: mutable.LinkedHashMap[Int, IdWithActor] = collection.mutable.LinkedHashMap[Int, IdWithActor]()

  override def receive: Receive = {
    case UserJoined(id, actor) =>
      users += (id -> IdWithActor(id, actor))
    case UserLeft(id) =>
      users -= id
    case PublicationAdded(publication) => notifyPublicationAdded(publication)
    case MessageRecived(message) =>
      println(message)
  }

  def notifyPublicationAdded(publication: Publication): Unit = {
    users.values.foreach(_.actor ! PublicationAdded(publication))
  }

}

