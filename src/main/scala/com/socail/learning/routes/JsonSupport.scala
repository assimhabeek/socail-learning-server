package com.socail.learning.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.socail.learning.domain.UserDomain.User
import spray.json.RootJsonFormat

trait JsonSupport extends SprayJsonSupport {
  import spray.json.DefaultJsonProtocol._
  implicit val userJsonFormat: RootJsonFormat[User] = jsonFormat4(User)

}
