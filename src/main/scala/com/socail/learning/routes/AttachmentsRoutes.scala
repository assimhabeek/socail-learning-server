package com.socail.learning.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.socail.learning.SocialLearningServer.config
import com.socail.learning.domain.Attachment
import com.socail.learning.repositories.AttachmentsRepository
import com.socail.learning.util.{AuthenticationHandler, JsonSupport}
import com.socail.learning.util.SLProtocal._

import scala.concurrent.ExecutionContext.Implicits.global

object AttachmentsRoutes extends JsonSupport with AuthenticationHandler {


  lazy val attachmentsRepo = new AttachmentsRepository(config)

  lazy val routes: Route =
    path("attachments") {
      concat(
        get {
          parameter('publicationId) { publicationId =>
            complete((StatusCodes.OK, attachmentsRepo.findWithPublication(toInt(publicationId).getOrElse(0))))
          }
        },
        authenticatedUser { user =>
          concat(
            post {
              entity(as[Attachment]) { attachment =>
                complete((StatusCodes.OK, attachmentsRepo.insert(attachment).map(x => s"${x.get}")))
              }
            },
            put {
              entity(as[Attachment]) { attachment =>
                complete((StatusCodes.OK, attachmentsRepo.update(attachment.id.getOrElse(0), attachment).map(x => s"$x")))
              }
            },
            delete {
              parameter('id) { id =>
                complete((StatusCodes.OK, attachmentsRepo.delete(toInt(id).getOrElse(0)).map(x => s"$x")))
              }
            },
          )

        })
    }

}

