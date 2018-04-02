package com.socail.learning.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.socail.learning.SocialLearningServer.config
import com.socail.learning.domain.{Comment, User}
import com.socail.learning.repositories.CommentsRepository
import com.socail.learning.util.{AuthenticationHandler, JsonSupport}
import com.socail.learning.util.SLProtocal._
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global

object CommentsRoutes extends JsonSupport with AuthenticationHandler {

  lazy val commentsRepo = new CommentsRepository(config)

  lazy val routes: Route =
    path("comments") {
      concat(
        get {
          parameter('publicationId) { publicationId =>
            complete((StatusCodes.OK, commentsRepo.findWithPublication(toInt(publicationId).getOrElse(0)) map {
              _.map(t => JsObject(t._1.toJson.asJsObject.fields + ("user" -> t._2.toJson.asJsObject)))
            }))
          }
        },
        authenticatedUser { user =>
          concat(
            post {
              entity(as[Comment]) { comment =>
                complete((StatusCodes.OK, commentsRepo.insert(comment).map(x => s"$x")))
              }
            },
            put {
              entity(as[Comment]) { comment =>
                complete((StatusCodes.OK, commentsRepo.update(comment.id.getOrElse(0), comment).map(x => s"$x")))
              }
            },
            delete {
              parameter('id) { id =>
                complete((StatusCodes.OK, commentsRepo.delete(toInt(id).getOrElse(0)).map(x => s"$x")))
              }
            },
          )
        }
      )
    }

}

