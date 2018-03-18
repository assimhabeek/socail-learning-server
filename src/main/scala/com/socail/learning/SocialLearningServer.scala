package com.socail.learning

import javax.mail.internet.InternetAddress

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.socail.learning.domain.DomainConfig.DbConfiguration
import com.socail.learning.routes.{CorsSupport, AuthRoutes}
import com.socail.learning.util.Mail
import courier.Text

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object SocialLearningServer extends App
  with DbConfiguration
  with AuthRoutes
  with CorsSupport {

  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  lazy val routes: Route = userRoutes
/*
  val address = new InternetAddress("assem.hebik@univ-constantine2.dz")
*/

  Http().bindAndHandle(corsHandler(routes), "0.0.0.0", 8080)
  println(s"Server online at http://localhost:8080/")
  Await.result(system.whenTerminated, Duration.Inf)

}
