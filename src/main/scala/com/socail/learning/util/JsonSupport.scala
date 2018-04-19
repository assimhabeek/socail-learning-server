package com.socail.learning.util

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.socail.learning.domain._
import spray.json._

trait JsonSupport extends SprayJsonSupport {

  import SLProtocal._

  implicit val specialtyJsonFormat: RootJsonFormat[Specialty] = jsonFormat5(Specialty)
  implicit val categoryJsonFormat: RootJsonFormat[Category] = jsonFormat4(Category)
  implicit val moduleJsonFormat: RootJsonFormat[Module] = jsonFormat6(Module)
  implicit val attachmentJsonFormat: RootJsonFormat[Attachment] = jsonFormat4(Attachment)
  implicit val commentJsonFormat: RootJsonFormat[Comment] = jsonFormat7(Comment)
  implicit val publicationJsonFormat: RootJsonFormat[Publication] = jsonFormat9(Publication)

  def toInt(s: String): Option[Int] = {
    try {
      Some(s.toInt)
    } catch {
      case e: NumberFormatException => None
    }
  }

}

