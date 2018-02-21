package com.socail.learning.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import com.socail.learning.SocialLearningServer.config
import com.socail.learning.repositories.UsersRepository


trait UserRoutes extends JsonSupport {
  import spray.json.DefaultJsonProtocol._

  lazy val usersRepo = new UsersRepository(config)
  lazy val userRoutes: Route =
    pathPrefix("users") {
      concat(
        pathEnd {
          concat(
            get {
              val users = usersRepo.findAll()
              complete(users)
            }
          )
        },
      )
    }
}
