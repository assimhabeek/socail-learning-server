package com.socail.learning

import java.io.InputStream
import java.security.{ KeyStore, SecureRandom }
import javax.net.ssl.{ KeyManagerFactory, SSLContext, TrustManagerFactory }

import akka.actor.ActorSystem
import akka.http.scaladsl.{ ConnectionContext, Http, HttpsConnectionContext }
import akka.http.scaladsl.server.Directives.concat
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.socail.learning.domain.DomainConfig.DbConfiguration
import com.socail.learning.routes._
import com.socail.learning.schema.InitSchema
import com.socail.learning.util.CorsSupport
import com.typesafe.sslconfig.akka.AkkaSSLConfig

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.StdIn

object SocialLearningServer extends App
    with DbConfiguration
    with CorsSupport {
  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  val sslConfig = AkkaSSLConfig(system)
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  implicit val executionContext = system.dispatcher

  lazy val routes: Route =
    concat(
      PublicationsRoutes.routes,
      FileRoutes.routes,
      AuthRoutes.routes,
      SpecialtiesRoutes.routes,
      ModulesRoutes.routes,
      CategoriesRoutes.routes,
      AttachmentsRoutes.routes,
      CommentsRoutes.routes,
      ChatRoutes.routes,
      OpinionsRoutes.routes,
      FriendsRoutes.routes
    )

  new InitSchema(config).init()
  val ks: KeyStore = KeyStore.getInstance("PKCS12")
  val keystore: InputStream = getClass.getClassLoader.getResourceAsStream("keystore.pkcs12")
  val password: Array[Char] = "123456789".toCharArray
  ks.load(keystore, password)
  val keyManagerFactory: KeyManagerFactory = KeyManagerFactory.getInstance("SunX509")
  keyManagerFactory.init(ks, password)

  val tmf: TrustManagerFactory = TrustManagerFactory.getInstance("SunX509")
  tmf.init(ks)

  val sslContext: SSLContext = SSLContext.getInstance("TLS")
  sslContext.init(keyManagerFactory.getKeyManagers, tmf.getTrustManagers, new SecureRandom)
  val https: HttpsConnectionContext = ConnectionContext.https(sslContext)

  val httpsBinding = Http().bindAndHandle(corsHandler(routes), "0.0.0.0", 8080, connectionContext = https)
  println(s"Server online at https://localhost:8080/")
  StdIn.readLine()
  httpsBinding.flatMap(_.unbind()).onComplete(_ => system.terminate())
}