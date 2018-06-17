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
import com.socail.learning.repositories.{PublicationsRepository, UsersRepository}
import com.socail.learning.util.SLProtocal._
import com.socail.learning.util.{AuthenticationHandler, JsonSupport}
import spray.json._
import com.socail.learning.SocialLearningServer.system
import com.socail.learning.actor._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PublicationsRoutes extends JsonSupport with AuthenticationHandler {

  val publicationAreaActor: ActorRef = system.actorOf(Props(new PublicationAreaActor()))
  val userActorSource: Source[PublicationEvent, ActorRef] = Source.actorRef[PublicationEvent](5, OverflowStrategy.fail)

  lazy val publicationsRepo = new PublicationsRepository(config)
  lazy val usersRepo = new UsersRepository(config)

  var userId = 0
  lazy val routes: Route =
    pathPrefix("publications") {
      concat(
        get {
          concat(
            parameter('page, 'filter.?, 'date.?, 'category.?, 'specialty.?, 'module.?, 'year.?) {
              (page, filter, date, category, specialty, module, year) =>
                val databaseRecords: Future[(Int, Seq[JsObject])] =
                  publicationsRepo.findByPage(toInt(page).getOrElse(0), filter, date, category, specialty, module, year) map { s =>
                    (s._1, s._2.map(t => JsObject(t._1.toJson.asJsObject.fields + ("user" -> t._2.toJson.asJsObject) + ("likes" -> JsNumber(t._3)) + ("dislikes" -> JsNumber(t._4)))))
                  }
                complete((StatusCodes.OK, databaseRecords))
            },
            parameter('id) { id =>
              complete((StatusCodes.OK, publicationsRepo.findById(toInt(id).getOrElse(0))))
            },
            parameter('user) { userId =>
              complete((StatusCodes.OK, publicationsRepo.findByUser(toInt(userId).getOrElse(0))))
            },
            parameter('eid) { id =>
              complete((StatusCodes.OK, publicationsRepo.findByIdExtanded(toInt(id).getOrElse(0)) map {
                _.map(t => JsObject(t._1.toJson.asJsObject.fields + ("user" -> t._2.toJson.asJsObject) + ("likes" -> JsNumber(t._3)) + ("dislikes" -> JsNumber(t._4))))
              }))
            },
            pathSuffix("reported") {
              complete((StatusCodes.OK, publicationsRepo.findReported()))
            },
            pathSuffix("stream") {
              userId += 1
              handleWebSocketMessages(flow(userId))
            },
            authenticatedUser { user =>
              parameter('fPage, 'filter.?, 'date.?, 'category.?, 'specialty.?, 'module.?, 'year.?) { (page, filter, date, category, specialty, module, year) =>
                complete((StatusCodes.OK, publicationsRepo.findByPageAndFriend(toInt(page).getOrElse(0), user.id.get, filter, date, category, specialty, module, year) map { s =>
                  (s._1, s._2.map(t => JsObject(t._1.toJson.asJsObject.fields + ("user" -> t._2.toJson.asJsObject) + ("likes" -> JsNumber(t._3)) + ("dislikes" -> JsNumber(t._4)))))
                }))
              }
            }

          )
        },
        authenticatedUser { user =>
          concat(
            post {
              entity(as[Publication]) { publication =>
                if (user.id.get == publication.userId) {
                  complete((StatusCodes.OK, publicationsRepo.insert(publication).map(x => {
                    usersRepo.findById(publication.userId)
                      .map(user => {
                        val us = user.get.toJson
                        val pub = publication.copy(id = Some(x)).toJson.asJsObject.fields
                        publicationAreaActor ! PublicationAddedOrUpdated(JsObject(pub + ("user" -> us.toJson)))
                        s"$x"
                      })
                  })))
                }
                else
                  complete(StatusCodes.Unauthorized, "")
              }
            },
            put {
              entity(as[Publication]) { publication =>
                if (user.id.get == publication.userId)
                  complete((StatusCodes.OK, publicationsRepo.update(publication.id.get, publication).map(x => {
                    usersRepo.findById(publication.userId)
                      .map(user => {
                        val us = user.get.toJson
                        val pub = publication.toJson.asJsObject.fields
                        publicationAreaActor ! PublicationAddedOrUpdated(JsObject(pub + ("user" -> us.toJson)))
                        s"$x"
                      })
                  })))
                else
                  complete(StatusCodes.Unauthorized, "")
              }
            },
            delete {
              parameter('id) { id =>
                val convId = toInt(id).getOrElse(0)
                complete((StatusCodes.OK, publicationsRepo.delete(convId).map(x => {
                  publicationAreaActor ! PublicationDeleted(convId)
                  s"$x"
                }
                )))
              }
            },
          )
        })
    }

  def flow(id: Int): Flow[Message, Message, Any] = Flow.fromGraph(GraphDSL.create(userActorSource) {
    implicit builder =>
      userActor =>
        import GraphDSL.Implicits._

        val materialization = builder.materializedValue.map(userActorRef => PublicationUserJoined(id, userActorRef))
        val publicationEventsToMessagesFlow = builder.add(Flow[PublicationEvent].map {
          case PublicationAddedOrUpdated(publication) => TextMessage(publication.toJson.toString)
          case PublicationDeleted(id) => TextMessage(s"$id")
        })

        val merge = builder.add(Merge[PublicationEvent](2))


        val publicationAreaActorSink: Sink[PublicationEvent, NotUsed] = Sink.actorRef[PublicationEvent](publicationAreaActor, PublicationUserLeft(id))

        val messagesToPublicationEventsFlow = builder.add(Flow[Message].collect {
          case TextMessage.Strict(message) => PublicationMessageReceived(message)
        })


        materialization ~> merge ~> publicationAreaActorSink
        messagesToPublicationEventsFlow ~> merge
        userActor ~> publicationEventsToMessagesFlow

        FlowShape(messagesToPublicationEventsFlow.in, publicationEventsToMessagesFlow.out)
  })
}



