package com.socail.learning.util

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.socail.learning.actor.{ CallRequest, CallerIdReceived, ChatMessage }
import com.socail.learning.domain._
import spray.json._

trait JsonSupport extends SprayJsonSupport {

  import SLProtocal._

  implicit val specialtyJsonFormat: RootJsonFormat[Specialty] = jsonFormat5(Specialty)
  implicit val categoryJsonFormat: RootJsonFormat[Category] = jsonFormat3(Category)
  implicit val moduleJsonFormat: RootJsonFormat[Module] = jsonFormat6(Module)
  implicit val attachmentJsonFormat: RootJsonFormat[Attachment] = jsonFormat4(Attachment)
  implicit val commentJsonFormat: RootJsonFormat[Comment] = jsonFormat6(Comment)
  implicit val opinionsJsonFormat: RootJsonFormat[Opinion] = jsonFormat5(Opinion)
  implicit val publicationJsonFormat: RootJsonFormat[Publication] = jsonFormat9(Publication)
  implicit val friendJsonFormat: RootJsonFormat[Friend] = jsonFormat4(Friend)
  implicit val chatRequestJsonFormat: RootJsonFormat[CallRequest] = jsonFormat4(CallRequest)
  implicit val chatMessageReceivedJsonFormat: RootJsonFormat[CallerIdReceived] = jsonFormat5(CallerIdReceived)
  implicit val roomJsonFormat: RootJsonFormat[Room] = jsonFormat5(Room)
  implicit val chatJsonFormat: RootJsonFormat[Chat] = jsonFormat5(Chat)
  implicit val chatMessageJsonFormat: RootJsonFormat[ChatMessage] = jsonFormat2(ChatMessage)

  def toInt(s: String): Option[Int] = {
    try {
      Some(s.toInt)
    } catch {
      case e: NumberFormatException => None
    }
  }

}

