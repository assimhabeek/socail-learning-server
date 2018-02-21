package com.socail.learning

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.socail.learning.domain.Domain.DbConfiguration
import com.socail.learning.repositorie.{ AddressesRepository, UsersRepository }

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object SocialLearningServer extends App with DbConfiguration {
  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val users = new UsersRepository(config)
  val address = new AddressesRepository(config)
  users.init()
  address.init()

  lazy val routes: Route = null
  Http().bindAndHandle(routes, "localhost", 8080)
  println(s"Server online at http://localhost:8080/")
  Await.result(system.whenTerminated, Duration.Inf)

}
