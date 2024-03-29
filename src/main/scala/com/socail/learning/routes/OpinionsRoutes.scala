package com.socail.learning.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.socail.learning.SocialLearningServer.config
import com.socail.learning.domain.Opinion
import com.socail.learning.repositories.{ OpinionsRepository, PublicationsRepository }
import com.socail.learning.util.{ AuthenticationHandler, JsonSupport }
import com.socail.learning.util.SLProtocal._
import spray.json.{ JsNumber, JsObject }

import scala.concurrent.ExecutionContext.Implicits.global

object OpinionsRoutes extends JsonSupport with AuthenticationHandler {

  lazy val opinionsRepo = new OpinionsRepository(config)
  lazy val publicationRepo = new PublicationsRepository(config)

  lazy val routes: Route =
    path("opinions") {
      authenticatedUser { user =>
        concat(
          get {
            parameter('publicationId) { pu =>
              complete((StatusCodes.OK, opinionsRepo.findByUserAndPublication(user.id.get, toInt(pu).getOrElse(0))))
            }
          },
          post {
            entity(as[Opinion]) { opinion =>
              if (user.id.get == opinion.userId)
                complete((StatusCodes.OK, opinionsRepo.insertOrUpdate(opinion).map(x => {
                  publicationRepo.deleteFrequentlyReported()
                  JsObject("likes" -> JsNumber(x._1), "dislikes" -> JsNumber(x._2))
                })))
              else
                complete((StatusCodes.Unauthorized, ""))
            }
          }
        )
      }
    }

}