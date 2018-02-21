package com.socail.learning

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.socail.learning.domain.DomainConfig.DbConfiguration
import com.socail.learning.repositories.UsersRepository
import com.socail.learning.routes.UserRoutes

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object SocialLearningServer extends App
  with DbConfiguration
  with UserRoutes {

  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()



  lazy val routes: Route = userRoutes

  Http().bindAndHandle(routes, "localhost", 8080)
  println(s"Server online at http://localhost:8080/")
  Await.result(system.whenTerminated, Duration.Inf)

}
