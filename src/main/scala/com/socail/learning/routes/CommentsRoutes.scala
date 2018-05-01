package com.socail.learning.routes

import akka.NotUsed
import akka.actor.Props
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.scaladsl._
import akka.stream.{FlowShape, OverflowStrategy}
import com.socail.learning.SocialLearningServer.{config, system}
import com.socail.learning.actor._
import com.socail.learning.domain.Comment
import com.socail.learning.repositories.{CommentsRepository, PublicationsRepository, UsersRepository}
import com.socail.learning.util.SLProtocal._
import com.socail.learning.util.{AuthenticationHandler, JsonSupport}
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global

object CommentsRoutes extends JsonSupport with AuthenticationHandler {

  val commentAreaActor = system.actorOf(Props(new CommentAreaActor()))
  val userActorSource = Source.actorRef[CommentEvent](5, OverflowStrategy.fail)
  lazy val usersRepo = new UsersRepository(config)
  lazy val publicationsRepo = new PublicationsRepository(config)

  lazy val commentsRepo = new CommentsRepository(config)
  var userId = 0

  lazy val routes: Route =
    pathPrefix("comments") {
      concat(
        get {
          parameter('publicationId) { publicationId =>
            concat(
              pathSuffix("stream") {
                handleWebSocketMessages(flow(userId, toInt(publicationId).getOrElse(0)))
              },
              pathEnd {
                complete((StatusCodes.OK, commentsRepo.findWithPublication(toInt(publicationId).getOrElse(0)) map {
                  _.map(t => JsObject(t._1.toJson.asJsObject.fields + ("user" -> t._2.toJson.asJsObject)))
                }))
              }
            )
          }
        },
        authenticatedUser { user =>
          concat(
            post {
              concat(
                pathSuffix("markBest") {
                  parameter('publicationId, 'commentId) { (pubId, commId) =>
                    val publId = toInt(pubId).getOrElse(0)
                    val currentUserId = user.id.get
                    complete(publicationsRepo.findById(publId).map {
                      case Some(pub) => pub.userId match {
                        case `currentUserId` =>
                          commentsRepo.markAsBes(publId, toInt(commId).getOrElse(0))
                          (StatusCodes.OK, "")
                        case _ => (StatusCodes.Unauthorized, "")
                      }
                      case None => (StatusCodes.NotFound, "")
                    })
                  }
                },
                pathEnd {

                  entity(as[Comment]) { comment =>
                    if (user.id.get == comment.userId)
                      complete((StatusCodes.OK, commentsRepo.insert(comment).map(x => {
                        usersRepo.findById(comment.userId)
                          .map(use => {
                            val us = use.get.toJson
                            val com = comment.toJson.asJsObject.fields
                            commentAreaActor ! CommentAddedOrUpdated(JsObject(com + ("user" -> us.toJson)))
                            s"$x"
                          })
                      })))
                    else
                      complete(StatusCodes.Unauthorized, "")
                  }
                }
              )
            },
            put {
              entity(as[Comment]) { comment =>
                complete((StatusCodes.OK, commentsRepo.update(comment.id.getOrElse(0), comment).map(x => s"$x")))
              }
            },
            delete {
              parameter('id) { id =>
                val convId = toInt(id).getOrElse(0)
                complete((StatusCodes.OK, commentsRepo.delete(convId).map(x => {
                  commentAreaActor ! CommentDeleted(convId)
                  s"$x"
                })))
              }
            },
          )
        }
      )
    }

  def flow(id: Int, pubId: Int): Flow[Message, Message, Any] = Flow.fromGraph(GraphDSL.create(userActorSource) {
    implicit builder =>
      userActor =>
        import GraphDSL.Implicits._

        val materialization = builder.materializedValue.map(userActorRef => CommentUserJoined(id, pubId, userActorRef))
        val commentEventsToMessagesFlow = builder.add(Flow[CommentEvent].map {
          case CommentAddedOrUpdated(comment) => TextMessage(comment.toJson.toString)
          case CommentDeleted(id) => TextMessage(s"$id")
        })

        val merge = builder.add(Merge[CommentEvent](2))


        val commentAreaActorSink: Sink[CommentEvent, NotUsed] = Sink.actorRef[CommentEvent](commentAreaActor, CommentUserLeft(id))

        val messagesToCommentEventsFlow = builder.add(Flow[Message].collect {
          case TextMessage.Strict(message) => CommentMessageReceived(message)
        })


        materialization ~> merge ~> commentAreaActorSink
        messagesToCommentEventsFlow ~> merge
        userActor ~> commentEventsToMessagesFlow

        FlowShape(messagesToCommentEventsFlow.in, commentEventsToMessagesFlow.out)
  })

}

