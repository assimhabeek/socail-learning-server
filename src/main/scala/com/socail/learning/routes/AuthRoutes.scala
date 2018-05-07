package com.socail.learning.routes

import com.socail.learning.SocialLearningServer.config
import com.socail.learning.util.{AuthenticationHandler, JsonSupport, Mail}
import com.socail.learning.domain.{Specialty, User}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import Directives.{entity, parameters, path, _}
import com.socail.learning.repositories.UsersRepository
import spray.json._

import com.socail.learning.util.SLProtocal._

import org.mindrot.jbcrypt.BCrypt

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AuthRoutes extends JsonSupport with AuthenticationHandler {

  lazy val usersRepo = new UsersRepository(config)

  lazy val routes: Route =
    concat(
      path("login") {
        post {
          entity(as[User]) { (user: User) =>
            complete(usersRepo.findBy(user.username, user.password) map {
              case Some(dbUser) => (StatusCodes.OK, createLoginToken(dbUser))
              case _ => (StatusCodes.Unauthorized, "WRONG_USERNAME_PASSWORD")
            })
          }
        }
      },
      pathPrefix("register") {
        concat(
          post {
            entity(as[User]) { (user: User) =>
              complete(usersRepo.insert(user) map {
                x => {
                  usersRepo.findById(x) map {
                    case Some(use) => (StatusCodes.OK, createLoginToken(use))
                    case _ => (StatusCodes.InternalServerError, "")
                  }
                }
              })
            }
          },
          get {
            concat(
              parameters('username) { username =>
                complete(usersRepo.findByUsername(username) map {
                  case Some(user) => (StatusCodes.OK, s"${user.id.get}")
                  case _ => (StatusCodes.OK, "0")
                })
              },
              parameters('email) { email =>
                complete(usersRepo.findByEmail(email) map {
                  case Some(user) => (StatusCodes.OK, s"${user.id.get}")
                  case _ => (StatusCodes.OK, "0")
                })
              },
              authenticatedUser { user =>
                concat(
                  parameters('token) { token =>
                    complete(if (validateRegistrationToken(token)) {
                      usersRepo.validateAccount(user.id.get) map { id => (StatusCodes.OK, createLoginToken(user.copy(verified = Some(true)))) }
                    } else (StatusCodes.Unauthorized, "TOKEN_INVALID"))
                  },
                  path("sendEmail") {
                    complete(usersRepo.sendRegistrationEmail(user.id.getOrElse(0), user.email.getOrElse("0"))
                      map { _ => (StatusCodes.OK, "Done") })
                  })
              }
            )
          },

        )
      },
      pathPrefix("password") {
        concat(
          get {
            parameters('email) { email =>
              val user: Future[Option[User]] = usersRepo.findByEmail(email)
              complete(user map {
                case Some(use) =>
                  usersRepo.sendPasswordRecoveryEmail(use.id.getOrElse(0), email)
                  (StatusCodes.OK, "Done")
                case _ => (StatusCodes.NotFound, "USER_NOT_FOUND")
              })
            }
          },
          put {
            authenticatedUser { user =>
              case class Password(newPassword: String, oldPassword: String)
              implicit val passwordJsonFormat: RootJsonFormat[Password] = jsonFormat2(Password)
              entity(as[Password]) { password: Password =>
                complete(usersRepo.findById(user.id.get).flatMap(x => {
                  if (BCrypt.checkpw(password.oldPassword, x.get.password)) usersRepo.changeUserPassword(user.id.get, password.newPassword)
                    .map(d => {
                      (StatusCodes.OK, s"$d")
                    }) else {
                    Future {
                      (StatusCodes.Unauthorized, "")
                    }
                  }
                }))
              }
            }
          }
        )
      },
      pathPrefix("users") {
        concat(
          get {
            concat(
             pathSuffix("all"){
               complete((StatusCodes.OK,usersRepo.findAll()))
             },
             isLoggedIn {user =>
                complete((StatusCodes.OK, usersRepo.findById(user.id.getOrElse(0))))
             }
            )
          },
          authenticatedUser { authUser =>
            concat(
              put {
                entity(as[User]) {
                  user =>
                    complete(
                      if (user.id == authUser.id) {
                        usersRepo.updateUserInfo(user) map {
                          id =>
                            (StatusCodes.OK, s"$id")
                        }
                      } else {
                        (StatusCodes.Unauthorized, "")
                      }
                    )
                }
              },
              delete {
                complete(StatusCodes.OK, s"${usersRepo.delete(authUser.id.getOrElse(0))}")
              }
            )
          })
      }
    )
}

