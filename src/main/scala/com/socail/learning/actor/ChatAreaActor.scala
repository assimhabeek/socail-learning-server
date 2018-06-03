package com.socail.learning.actor

import java.sql.Timestamp

import akka.actor.{ Actor, ActorRef }
import com.socail.learning.domain.Chat
import com.socail.learning.SocialLearningServer.config
import com.socail.learning.repositories.ChatsRepository

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ChatEvent

object ChatEvent {
  val CALL_REQUESTED = 0
  val CALL_ACCEPTED = 1
  val CALL_REJECTED = 2
  val CALL_RECEIVED = 3
  val CHAT_RECEIVED = 4
}

case class ChatUserJoined(id: Int, actorRef: ActorRef) extends ChatEvent

case class ChatUserLeft(id: Int) extends ChatEvent

trait CallEventRequest extends ChatEvent {
  val id: Int
  val senderId: Int
  val receiverId: Int
  val eventType: Int
}

case class CallRequest(
  id: Int,
  senderId: Int,
  receiverId: Int,
  eventType: Int
) extends CallEventRequest

case class CallerIdReceived(
  id: Int,
  senderId: Int,
  receiverId: Int,
  eventType: Int,
  callerId: String
) extends CallEventRequest

case class ChatIdWithActor(id: Int, actor: ActorRef)

case class ChatMessage(message: Chat, eventType: Int) extends ChatEvent

class ChatAreaActor extends Actor {

  val users: mutable.LinkedHashMap[Int, ChatIdWithActor] = collection.mutable.LinkedHashMap[Int, ChatIdWithActor]()
  val rooms: mutable.LinkedHashMap[Int, Int] = collection.mutable.LinkedHashMap[Int, Int]()
  val waiting: mutable.LinkedHashMap[Int, Int] = collection.mutable.LinkedHashMap[Int, Int]()
  val chatRepo = new ChatsRepository(config)

  override def receive: Receive = {
    case ChatUserJoined(id, actor) =>
      users += (id -> ChatIdWithActor(id, actor))
    case ChatUserLeft(id) =>
      users -= id
    case CallRequest(id, senderId, receiverId, eventType) =>
      eventType match {
        case ChatEvent.CALL_REQUESTED =>
          users.values.find(_.id == receiverId) match {
            case Some(user) => {
              user.actor ! CallRequest(id, senderId, receiverId, ChatEvent.CALL_REQUESTED)
            }
            case None => self ! CallRequest(id, senderId, receiverId, ChatEvent.CALL_REJECTED)
          }
        case ChatEvent.CALL_ACCEPTED =>
          rooms += (senderId -> receiverId)
          users.values.find(_.id == senderId) match {
            case Some(user) => user.actor ! CallRequest(id, senderId, receiverId, ChatEvent.CALL_ACCEPTED)
            case None => self ! CallRequest(id, senderId, receiverId, ChatEvent.CALL_REJECTED)
          }
        case ChatEvent.CALL_REJECTED =>
          rooms -= senderId
          waiting -= senderId
          users.values.find(_.id == senderId) match {
            case Some(user) => user.actor ! CallRequest(id, senderId, receiverId, ChatEvent.CALL_REJECTED)
            case None => self ! CallRequest(id, senderId, receiverId, ChatEvent.CALL_REJECTED)
          }
      }
    case CallerIdReceived(id, senderId, receiverId, eventType, message) =>
      rooms.find(x => (x._1 == senderId && x._2 == receiverId) || (x._2 == senderId && x._1 == receiverId))
        .foreach(room => users.find(x => x._1 == receiverId)
          .foreach(_._2.actor ! CallerIdReceived(id, senderId, receiverId, eventType, message)))
    case ChatMessage(message: Chat, eventType: Int) =>
      users.foreach(x => chatRepo.isRegistredToRoom(message.roomId, x._1).map(y => {
        if (y)
          x._2.actor ! ChatMessage(message, eventType)
      }))
  }

}

