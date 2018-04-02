package com.socail.learning.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.socail.learning.SocialLearningServer.config
import com.socail.learning.domain.Module
import com.socail.learning.repositories.ModulesRepository
import com.socail.learning.util.{AuthenticationHandler, JsonSupport}
import com.socail.learning.util.SLProtocal._

import scala.concurrent.ExecutionContext.Implicits.global

object ModulesRoutes extends JsonSupport with AuthenticationHandler {

  lazy val modulesRepo = new ModulesRepository(config)

  lazy val routes: Route =
    path("modules") {
      concat(
        get {
          complete((StatusCodes.OK, modulesRepo.findAll()))
        },
        authenticatedAdmin { admin =>
          concat(
            post {
              entity(as[Module]) { module =>
                complete((StatusCodes.OK, modulesRepo.insert(module).map(x => s"$x")))
              }
            },
            put {
              entity(as[Module]) { module =>
                complete((StatusCodes.OK, modulesRepo.update(module.id.getOrElse(0), module).map(x => s"$x")))
              }
            },
            delete {
              parameter('id) { id =>
                complete((StatusCodes.OK, modulesRepo.delete(toInt(id).getOrElse(0)).map(x => s"$x")))
              }
            },
          )
        }
      )
    }
}

