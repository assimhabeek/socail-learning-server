package com.socail.learning.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.socail.learning.SocialLearningServer.config
import com.socail.learning.domain.Specialty
import com.socail.learning.repositories.SpecialtiesRepository
import com.socail.learning.util.{AuthenticationHandler, JsonSupport}
import com.socail.learning.util.SLProtocal._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SpecialtiesRoutes extends JsonSupport with AuthenticationHandler {

  lazy val specialtiesRepo = new SpecialtiesRepository(config)

  lazy val routes: Route =
    pathPrefix("specialties") {
      concat(
        get {
          concat(
            parameter('year) { year =>
              complete(StatusCodes.OK, specialtiesRepo.findByYear(toInt(year).getOrElse(0)))

            },
            pathEnd {
              complete(StatusCodes.OK, specialtiesRepo.findAll())
            })
        },
        authenticatedUser { user =>
          concat(
            post {
              entity(as[Specialty]) { specialty =>
                complete((StatusCodes.OK, specialtiesRepo.insert(specialty).map(x => s"$x")))
              }
            },
            put {
              entity(as[Specialty]) { specialty =>
                complete((StatusCodes.OK, specialtiesRepo.update(specialty.id.getOrElse(0), specialty).map(x => s"$x")))
              }
            },
            delete {
              parameter('id) { id =>
                complete((StatusCodes.OK, specialtiesRepo.delete(toInt(id).getOrElse(0)).map(x => s"$x")))
              }
            },
          )
        }
      )
    }

}

