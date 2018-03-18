package com.socail.learning.routes

import javax.mail.internet.InternetAddress

import com.socail.learning.SocialLearningServer.config
import com.socail.learning.util.{AuthenticationHandler, Mail}
import com.socail.learning.domain.User
import akka.http.scaladsl.model._
import StatusCodes._
import akka.http.scaladsl.server._
import Directives._
import com.socail.learning.repositories.{SpecialtiesRepository, UsersRepository}
import courier.Text

import scala.concurrent.ExecutionContext.Implicits.global

trait AuthRoutes extends JsonSupport with AuthenticationHandler {

  lazy val usersRepo = new UsersRepository(config)
  lazy val specaiRepo = new SpecialtiesRepository(config)
  usersRepo.insert(User(None, "root", "rootroot", None, None, None, None, None, Some(true)))

  lazy val userRoutes: Route =
    concat(
      path("login") {
        post {
          entity(as[User]) { (user: User) =>
            complete(usersRepo.findBy(user.username, user.password) map {
              case Some(dbUser) => HttpResponse(StatusCodes.OK, entity = createTokenWith(dbUser))
              case _ => HttpResponse(StatusCodes.Unauthorized, entity = "WRONG_USERNAME_PASSWORD")
            })
          }
        }
      },
      path("register") {
        post {
          entity(as[User]) { (user: User) =>
            complete(usersRepo.insert(user) map {
              x => HttpResponse(StatusCodes.OK, entity = s"$x")
            })
          }
        }
      }
    )

}

