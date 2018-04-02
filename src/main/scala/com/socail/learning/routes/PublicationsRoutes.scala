package com.socail.learning.routes

import java.sql.Timestamp

import akka.NotUsed
import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.TextMessage.Strict
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.{FlowShape, OverflowStrategy, SourceShape}
import akka.stream.scaladsl._
import com.socail.learning.SocialLearningServer.config
import com.socail.learning.domain.{Publication, User}
import com.socail.learning.repositories.PublicationsRepository
import com.socail.learning.util.SLProtocal._
import com.socail.learning.util.{AuthenticationHandler, JsonSupport}
import spray.json._
import com.socail.learning.SocialLearningServer.system
import com.socail.learning.actor._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PublicationsRoutes extends JsonSupport with AuthenticationHandler {

  val publicationAreaActor = system.actorOf(Props(new PublicationAreaActor()))
  val userActorSource = Source.actorRef[PublicationEvent](5, OverflowStrategy.fail)
  lazy val publicationsRepo = new PublicationsRepository(config)
  var userId = 0
  lazy val routes: Route =
    pathPrefix("publications") {
      concat(
        get {
          concat(
            parameter('page) { page =>
              val databaseRecords: Future[Seq[JsObject]] = publicationsRepo.findByPage(toInt(page).getOrElse(0)) map {
                _.map(t => JsObject(t._1.toJson.asJsObject.fields + ("user" -> t._2.toJson.asJsObject)))
              }
              complete((StatusCodes.OK, databaseRecords))
            },
            pathSuffix("push") {
              publicationAreaActor ! PublicationAdded(Publication(None, "Test", Some("ads"), new Timestamp(2000, 1, 1, 9, 10, 20, 30), 1, None, 1, None, None))
              complete(StatusCodes.OK, "Yea")
            },
            pathSuffix("stream") {
              userId += 1
              handleWebSocketMessages(flow(userId))
            }
          )
        },
        authenticatedUser { user =>
          concat(
            post {
              entity(as[Publication]) { publication =>
                complete((StatusCodes.OK, publicationsRepo.insert(publication).map(x => s"$x")))
              }
            },
            put {
              entity(as[Publication]) { publication =>
                complete((StatusCodes.OK, publicationsRepo.update(publication.id.getOrElse(0), publication).map(x => s"$x")))
              }
            },
            delete {
              parameter('id) { id =>
                complete((StatusCodes.OK, publicationsRepo.delete(toInt(id).getOrElse(0)).map(x => s"$x")))
              }
            },
          )
        })
    }

  def flow(id: Int): Flow[Message, Message, Any] = Flow.fromGraph(GraphDSL.create(userActorSource) {
    implicit builder =>
      userActor =>
        import GraphDSL.Implicits._

        val materialization = builder.materializedValue.map(userActorRef => UserJoined(id, userActorRef))
        val publicationEventsToMessagesFlow = builder.add(Flow[PublicationEvent].map {
          case PublicationAdded(publication) => TextMessage(publication.toJson.toString)
        })

        val merge = builder.add(Merge[PublicationEvent](2))


        val publicationAreaActorSink: Sink[PublicationEvent, NotUsed] = Sink.actorRef[PublicationEvent](publicationAreaActor, UserLeft(id))

        val messagesToPublicationEventsFlow = builder.add(Flow[Message].collect {
          case TextMessage.Strict(message) => MessageRecived(message)
        })


        materialization ~> merge ~> publicationAreaActorSink
        messagesToPublicationEventsFlow ~> merge
        userActor ~> publicationEventsToMessagesFlow

        FlowShape(messagesToPublicationEventsFlow.in, publicationEventsToMessagesFlow.out)
  })
}



