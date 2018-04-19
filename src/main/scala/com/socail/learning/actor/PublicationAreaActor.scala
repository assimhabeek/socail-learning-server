package com.socail.learning.actor

import akka.actor.{ Actor, ActorRef }
import com.socail.learning.domain.Publication
import spray.json.JsValue

import scala.collection.mutable

trait PublicationEvent

case class PublicationUserJoined(id: Int, actorRef: ActorRef) extends PublicationEvent

case class PublicationUserLeft(id: Int) extends PublicationEvent

case class PublicationAddedOrUpdated(publication: JsValue) extends PublicationEvent

case class PublicationDeleted(id: Int) extends PublicationEvent

case class PublicationMessageReceived(message: String) extends PublicationEvent

case class PublicationIdWithActor(id: Int, actor: ActorRef)

class PublicationAreaActor extends Actor {

  val users: mutable.LinkedHashMap[Int, PublicationIdWithActor] = collection.mutable.LinkedHashMap[Int, PublicationIdWithActor]()

  override def receive: Receive = {
    case PublicationUserJoined(id, actor) =>
      users += (id -> PublicationIdWithActor(id, actor))
    case PublicationUserLeft(id) =>
      users -= id
    case PublicationAddedOrUpdated(publication) => notifyPublicationAdded(publication)
    case PublicationDeleted(id) => notifyPublicationDeleted(id)
    case PublicationMessageReceived(message) =>
      println(message)
  }

  def notifyPublicationAdded(publication: JsValue): Unit = {
    users.values.foreach(_.actor ! PublicationAddedOrUpdated(publication))
  }

  def notifyPublicationDeleted(id: Int): Unit = {
    users.values.foreach(_.actor ! PublicationDeleted(id))
  }

}

