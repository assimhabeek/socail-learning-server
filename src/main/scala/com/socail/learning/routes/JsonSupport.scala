package com.socail.learning.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.socail.learning.domain.{ BasicEntity, Specialty, User }
import spray.json.RootJsonFormat

trait JsonSupport extends SprayJsonSupport {

  import spray.json.DefaultJsonProtocol._

  implicit val userJsonFormat: RootJsonFormat[User] = jsonFormat9(User)
  implicit val specialtyJsonFormat: RootJsonFormat[Specialty] = jsonFormat5(Specialty)

}
