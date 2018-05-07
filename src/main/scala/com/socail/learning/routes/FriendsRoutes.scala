package com.socail.learning.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.socail.learning.SocialLearningServer.config
import com.socail.learning.domain.Friend
import com.socail.learning.repositories.FriendsRepository
import com.socail.learning.util.{ AuthenticationHandler, JsonSupport }
import com.socail.learning.util.SLProtocal._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object FriendsRoutes extends JsonSupport with AuthenticationHandler {

  lazy val friendsRepo = new FriendsRepository(config)
  // here it is very danger i should check if the user is either the sender or receiver
  lazy val routes: Route =
    authenticatedUser { user =>
      path("friends") {
        concat(
          get {
            complete((StatusCodes.OK, friendsRepo.findFriends(user.id.getOrElse(0))))
          },
          post {
            entity(as[Friend]) { friend =>
              complete((StatusCodes.OK, friendsRepo.insert(friend).map(x => s"$x")))
            }
          },
          put {
            entity(as[Friend]) { friend =>
              complete((StatusCodes.OK, friendsRepo.update(friend.id.getOrElse(0), friend).map(x => s"$x")))
            }
          },
          delete {
            parameter('id) { id =>
              complete((StatusCodes.OK, friendsRepo.delete(toInt(id).getOrElse(0)).map(x => s"$x")))
            }
          }
        )
      }
    }
}

