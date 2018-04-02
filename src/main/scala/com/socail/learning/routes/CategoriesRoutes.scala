package com.socail.learning.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.socail.learning.SocialLearningServer.config
import com.socail.learning.domain.Category
import com.socail.learning.repositories.CategoriesRepository
import com.socail.learning.util.{AuthenticationHandler, JsonSupport}
import com.socail.learning.util.SLProtocal._

import scala.concurrent.ExecutionContext.Implicits.global

object CategoriesRoutes extends JsonSupport with AuthenticationHandler {

  lazy val categoriesRepo = new CategoriesRepository(config)

  lazy val routes: Route =
    path("categories") {
      concat(
        get {
          complete((StatusCodes.OK, categoriesRepo.findAll()))
        },
        authenticatedAdmin { admin =>
          concat(
            post {
              entity(as[Category]) { category =>
                complete((StatusCodes.OK, categoriesRepo.insert(category).map(x => s"$x")))
              }
            },
            put {
              entity(as[Category]) { category =>
                complete((StatusCodes.OK, categoriesRepo.update(category.id.getOrElse(0), category).map(x => s"$x")))
              }
            },
            delete {
              parameter('id) { id =>
                complete((StatusCodes.OK, categoriesRepo.delete(toInt(id).getOrElse(0)).map(x => s"$x")))
              }
            },
          )
        }
      )
    }

}

