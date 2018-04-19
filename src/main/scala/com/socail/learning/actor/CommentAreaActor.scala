package com.socail.learning.actor

import akka.actor.{ Actor, ActorRef }
import com.socail.learning.domain.Comment
import spray.json.JsValue

import scala.collection.mutable

trait CommentEvent

case class CommentUserJoined(id: Int, pubId: Int, actorRef: ActorRef) extends CommentEvent

case class CommentUserLeft(id: Int) extends CommentEvent

case class CommentAddedOrUpdated(comment: JsValue) extends CommentEvent

case class CommentDeleted(id: Int) extends CommentEvent

case class CommentMessageReceived(message: String) extends CommentEvent

case class CommentIdWithActor(id: Int, actor: ActorRef)

class CommentAreaActor extends Actor {

  var users: mutable.MutableList[(Int, Int, CommentIdWithActor)] = mutable.MutableList()

  override def receive: Receive = {
    case CommentUserJoined(id, pubId, actor) =>
      users += ((id, pubId, CommentIdWithActor(id, actor)))
    case PublicationUserLeft(id) =>
      users = users.filter(x => x._1 != id)
    case CommentAddedOrUpdated(comment) => notifyCommentAdded(comment)
    case CommentDeleted(id) => notifyCommentDeleted(id)
    case PublicationMessageReceived(message) =>
      println(message)
  }

  def notifyCommentAdded(comment: JsValue): Unit = {
    users.filter(x => x._2 == comment.asJsObject.fields("publicationId").toString().toInt).foreach(_._3.actor ! CommentAddedOrUpdated(comment))
  }

  def notifyCommentDeleted(id: Int): Unit = {
    users.foreach(_._3.actor ! CommentDeleted(id))
  }

}

