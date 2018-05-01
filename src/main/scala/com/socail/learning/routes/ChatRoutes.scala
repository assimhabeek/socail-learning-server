package com.socail.learning.routes

import java.sql.Timestamp

import akka.NotUsed
import akka.actor.Props
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.http.scaladsl.server.Directives.{ path, _ }
import akka.http.scaladsl.server._
import akka.stream.scaladsl.{ Flow, GraphDSL, Merge, Sink, Source }
import akka.stream.{ FlowShape, OverflowStrategy }
import com.socail.learning.domain.Chat
import com.socail.learning.util.SLProtocal._
import com.socail.learning.util.{ AuthenticationHandler, JsonSupport }
import spray.json._
import com.socail.learning.SocialLearningServer.system
import com.socail.learning.actor._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ChatRoutes extends JsonSupport with AuthenticationHandler {

  val chatAreaActor = system.actorOf(Props(new ChatAreaActor()))
  val userActorSource = Source.actorRef[ChatEvent](5, OverflowStrategy.fail)

  lazy val routes: Route =
    wsAuthenticatedUser { user =>
      path("chat") {
        handleWebSocketMessages(flow(user.id.getOrElse(0)))
      }
    }

  def flow(id: Int): Flow[Message, Message, Any] = Flow.fromGraph(GraphDSL.create(userActorSource) { implicit builder => userActor =>
    import GraphDSL.Implicits._

    val materialization = builder.materializedValue.map(userActorRef => ChatUserJoined(id, userActorRef))

    val chatEventsToMessagesFlow = builder.add(Flow[ChatEvent].map {
      case x: ChatRequest => TextMessage(x.toJson.toString())
      case y: ChatMessageReceived => TextMessage(y.toJson.toString())
    })

    val merge = builder.add(Merge[ChatEvent](2))
    val chatAreaActorSink: Sink[ChatEvent, NotUsed] = Sink.actorRef[ChatEvent](chatAreaActor, ChatUserLeft(id))

    val messagesToChatEventsFlow = builder.add(Flow[Message].collect {
      case TextMessage.Strict(message) =>
        val messageObj = JsonParser(message).asJsObject
        messageObj.fields("eventType").toString().toInt match {
          case ChatEvent.CHAT_MESSAGE_RECEIVED => messageObj.convertTo[ChatMessageReceived]
          case _ => messageObj.convertTo[ChatRequest]
        }
    })

    materialization ~> merge ~> chatAreaActorSink
    messagesToChatEventsFlow ~> merge
    userActor ~> chatEventsToMessagesFlow

    FlowShape(messagesToChatEventsFlow.in, chatEventsToMessagesFlow.out)
  })
}

