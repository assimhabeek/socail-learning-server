package com.socail.learning.routes

import java.sql.Timestamp

import akka.NotUsed
import akka.actor.Props
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.http.scaladsl.server.Directives.{ path, _ }
import akka.http.scaladsl.server._
import akka.stream.scaladsl.{ Flow, GraphDSL, Merge, Sink, Source }
import akka.stream.{ FlowShape, OverflowStrategy }
import com.socail.learning.domain.{ Chat, Room }
import com.socail.learning.util.SLProtocal._
import com.socail.learning.util.{ AuthenticationHandler, JsonSupport }
import spray.json._
import com.socail.learning.SocialLearningServer.system
import com.socail.learning.actor._
import com.socail.learning.repositories.ChatsRepository
import com.socail.learning.SocialLearningServer.config
import jdk.net.SocketFlow.Status
import spray.json._
import com.socail.learning.util.SLProtocal._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ChatRoutes extends JsonSupport with AuthenticationHandler {

  val chatAreaActor = system.actorOf(Props(new ChatAreaActor()))
  val userActorSource = Source.actorRef[ChatEvent](5, OverflowStrategy.fail)
  val chatRepo = new ChatsRepository(config)
  lazy val routes: Route =
    concat(
      wsAuthenticatedUser { user =>
        path("chat") {
          handleWebSocketMessages(flow(user.id.getOrElse(0)))
        }
      },
      authenticatedUser { user =>
        concat(
          path("room") {
            concat(
              post {
                entity(as[Room]) { room =>
                  complete((StatusCodes.OK, chatRepo.createRoom(room).map(x => s"$x")))
                }
              },
              get {
                complete((StatusCodes.OK, chatRepo.getRoomsWithUsers(user.id.get)))
              }
            )
          },
          path("isRegistred") {
            parameter('roomId) { roomId =>
              get {
                complete((StatusCodes.OK, chatRepo.isRegistredToRoom(toInt(roomId).getOrElse(0), user.id.get).map(x => s"$x")))
              }
            }
          },
          pathPrefix("messages") {
            concat(
              get {
                concat(
                  pathSuffix("unRead") {
                    complete((StatusCodes.OK, chatRepo.getUnReadMessages(user.id.get)))
                  },
                  parameter('roomId) { roomId =>
                    val room = toInt(roomId).getOrElse(0)
                    chatRepo.makeRoomReadForUser(user.id.get, room)
                    complete((StatusCodes.OK, chatRepo.getMessages(room)))
                  }
                )
              },
              post {
                entity(as[Chat]) { chat =>
                  complete((StatusCodes.OK, chatRepo.insert(chat).map(x => {
                    chatAreaActor ! ChatMessage(chat, 4)
                    s"$x"
                  })))
                }
              }
            )
          },
          path("roomUsers") {
            concat(
              get {
                parameter('roomId) { roomId =>
                  complete((StatusCodes.OK, chatRepo.getRoomUsers(toInt(roomId).getOrElse(0), user.id.get)))
                }
              },
              post {
                parameter('room, 'user) { (room, user) =>
                  val roomId = toInt(room).getOrElse(0)
                  val userId = toInt(user).getOrElse(0)
                  complete((StatusCodes.OK, chatRepo.addToRoom(roomId, userId).map(x => s"$x")))
                }
              }
            )
          }
        )

      }
    )

  def flow(id: Int): Flow[Message, Message, Any] = Flow.fromGraph(GraphDSL.create(userActorSource) { implicit builder => userActor =>

    import GraphDSL.Implicits._

    val materialization = builder.materializedValue.map(userActorRef => ChatUserJoined(id, userActorRef))

    val chatEventsToMessagesFlow = builder.add(Flow[ChatEvent].map {
      case x: CallRequest => TextMessage(x.toJson.toString())
      case y: CallerIdReceived => TextMessage(y.toJson.toString())
      case z: ChatMessage => TextMessage(z.message.toJson.toString())
    })

    val merge = builder.add(Merge[ChatEvent](2))
    val chatAreaActorSink: Sink[ChatEvent, NotUsed] = Sink.actorRef[ChatEvent](chatAreaActor, ChatUserLeft(id))

    val messagesToChatEventsFlow = builder.add(Flow[Message].collect {
      case TextMessage.Strict(message) =>
        val messageObj = JsonParser(message).asJsObject
        messageObj.fields("eventType").toString().toInt match {
          case ChatEvent.CALL_RECEIVED => messageObj.convertTo[CallerIdReceived]
          case ChatEvent.CHAT_RECEIVED => messageObj.convertTo[ChatMessage]
          case _ => messageObj.convertTo[CallRequest]
        }
    })

    materialization ~> merge ~> chatAreaActorSink
    messagesToChatEventsFlow ~> merge
    userActor ~> chatEventsToMessagesFlow

    FlowShape(messagesToChatEventsFlow.in, chatEventsToMessagesFlow.out)
  })
}

