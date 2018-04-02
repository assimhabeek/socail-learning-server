package com.socail.learning.util

import java.sql.Timestamp

import com.socail.learning.domain.User
import spray.json._

object SLProtocal extends DefaultJsonProtocol {

  implicit object UserJsonFormat extends RootJsonFormat[User] {
    override def read(json: JsValue): User = {
      if (json.asJsObject.fields.keySet.contains("profileImage")) {
        val stringImage = json.asJsObject.fields("profileImage").toString()
        val byteImage: Array[Byte] = stringImage.getBytes()
        jsonFormat12(User).read(JsObject(json.asJsObject.fields + ("profileImage" -> byteImage.toJson)))
      } else {
        jsonFormat12(User).read(json)
      }
    }

    override def write(obj: User): JsValue = {
      obj.password = ""
      val base64Image = obj.profileImage.getOrElse(Array()).map(_.toChar).mkString.replaceAll("\"", "")
      val jsObj = jsonFormat12(User).write(obj)
      val objFields: Map[String, JsValue] = jsObj.asJsObject.fields
      JsObject(objFields + ("profileImage" -> base64Image.toJson))
    }
  }

  implicit object TimestampJsonFormat extends RootJsonFormat[Timestamp] {
    override def write(obj: Timestamp) = JsString(obj.toString)

    override def read(json: JsValue): Timestamp = json match {
      case JsString(s) => Timestamp.valueOf(s)
      case _ => throw DeserializationException("Invalid date format: " + json)
    }
  }

}