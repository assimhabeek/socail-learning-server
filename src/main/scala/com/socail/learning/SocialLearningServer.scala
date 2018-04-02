package com.socail.learning

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.concat
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.socail.learning.domain.DomainConfig.DbConfiguration
import com.socail.learning.routes._
import com.socail.learning.util.CorsSupport

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object SocialLearningServer extends App
    with DbConfiguration
    with CorsSupport {

  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  lazy val routes: Route =
    concat(
      AuthRoutes.routes,
      SpecialtiesRoutes.routes,
      ModulesRoutes.routes,
      CategoriesRoutes.routes,
      AttachmentsRoutes.routes,
      CommentsRoutes.routes,
      PublicationsRoutes.routes
    )

  /*
  new InitSchema(config).init()
*/

  Http().bindAndHandle(corsHandler(routes), "0.0.0.0", 8080)
  println(s"Server online at http://localhost:8080/")
  Await.result(system.whenTerminated, Duration.Inf)

}
