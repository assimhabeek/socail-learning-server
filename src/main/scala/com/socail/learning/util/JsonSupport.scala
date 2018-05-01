package com.socail.learning.util

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.socail.learning.actor.{ ChatMessageReceived, ChatRequest }
import com.socail.learning.domain._
import spray.json._

trait JsonSupport extends SprayJsonSupport {

  import SLProtocal._

  implicit val specialtyJsonFormat: RootJsonFormat[Specialty] = jsonFormat5(Specialty)
  implicit val categoryJsonFormat: RootJsonFormat[Category] = jsonFormat4(Category)
  implicit val moduleJsonFormat: RootJsonFormat[Module] = jsonFormat6(Module)
  implicit val attachmentJsonFormat: RootJsonFormat[Attachment] = jsonFormat4(Attachment)
  implicit val commentJsonFormat: RootJsonFormat[Comment] = jsonFormat6(Comment)
  implicit val opinionsJsonFormat: RootJsonFormat[Opinion] = jsonFormat5(Opinion)
  implicit val publicationJsonFormat: RootJsonFormat[Publication] = jsonFormat9(Publication)
  implicit val chatJsonFormat: RootJsonFormat[Chat] = jsonFormat5(Chat)
  implicit val chatRequestJsonFormat: RootJsonFormat[ChatRequest] = jsonFormat3(ChatRequest)
  implicit val chatMessageReceivedJsonFormat: RootJsonFormat[ChatMessageReceived] = jsonFormat2(ChatMessageReceived)

  def toInt(s: String): Option[Int] = {
    try {
      Some(s.toInt)
    } catch {
      case e: NumberFormatException => None
    }
  }

}

