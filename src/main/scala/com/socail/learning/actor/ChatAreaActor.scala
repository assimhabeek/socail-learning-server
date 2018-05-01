package com.socail.learning.actor

import java.sql.Timestamp

import akka.actor.{ Actor, ActorRef }
import com.socail.learning.domain.Chat

import scala.collection.mutable

trait ChatEvent

object ChatEvent {
  val CHAT_REQUESTED = 0
  val CHAT_ACCEPTED = 1
  val CHAT_REJECTED = 2
  val CHAT_MESSAGE_RECEIVED = 3
}

case class ChatUserJoined(id: Int, actorRef: ActorRef) extends ChatEvent

case class ChatUserLeft(id: Int) extends ChatEvent

case class ChatRequest(senderId: Int, receiverId: Int, eventType: Int) extends ChatEvent

case class ChatMessageReceived(message: Chat, eventType: Int) extends ChatEvent

case class ChatIdWithActor(id: Int, actor: ActorRef)

class ChatAreaActor extends Actor {

  val users: mutable.LinkedHashMap[Int, ChatIdWithActor] = collection.mutable.LinkedHashMap[Int, ChatIdWithActor]()
  val rooms: mutable.LinkedHashMap[Int, Int] = collection.mutable.LinkedHashMap[Int, Int]()

  override def receive: Receive = {
    case ChatUserJoined(id, actor) =>
      users += (id -> ChatIdWithActor(id, actor))
    case ChatUserLeft(id) =>
      users -= id
    case ChatRequest(senderId, receiverId, eventType) =>
      eventType match {
        case ChatEvent.CHAT_REQUESTED => users.values.find(_.id == receiverId) match {
          case Some(user) => user.actor ! ChatRequest(senderId, receiverId, ChatEvent.CHAT_REQUESTED)
          case None => self ! ChatRequest(senderId, receiverId, ChatEvent.CHAT_REJECTED)
        }
        case ChatEvent.CHAT_ACCEPTED =>
          rooms += (senderId -> receiverId)
          users.values.find(_.id == senderId) match {
            case Some(user) => user.actor ! ChatRequest(senderId, receiverId, ChatEvent.CHAT_ACCEPTED)
            case None => self ! ChatRequest(senderId, receiverId, ChatEvent.CHAT_REJECTED)
          }
        case ChatEvent.CHAT_REJECTED =>
          rooms -= senderId
          users.values.find(_.id == senderId) match {
            case Some(user) => user.actor ! ChatRequest(senderId, receiverId, ChatEvent.CHAT_REJECTED)
            case None => self ! ChatRequest(senderId, receiverId, ChatEvent.CHAT_REJECTED)
          }
      }
    case ChatMessageReceived(message, eventType) =>
      rooms.find(x => (x._1 == message.senderId && x._2 == message.receiverId) || (x._2 == message.senderId && x._1 == message.receiverId)) match {
        case Some(x) => users.values.filter(y => y.id == x._1 || y.id == x._2).foreach(_.actor ! ChatMessageReceived(message, eventType))
      }
  }

}

