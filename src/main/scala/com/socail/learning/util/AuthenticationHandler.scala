package com.socail.learning.util

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{complete, optionalHeaderValueByName, provide}
import com.socail.learning.domain.User
import io.igl.jwt._
import play.api.libs.json._

import scala.util.Try

trait AuthenticationHandler {
  val secretKey = "@#TNf_*34ASsad3164$%#$FFDDSAF15365"


  def decodeToken(token: String): Try[Jwt] = {
    val res = DecodedJwt.validateEncodedJwt(
      token,
      secretKey,
      Algorithm.HS256,
      Set(Typ),
      Set(Usr),
    )
    res
  }

  def validateLoginToken(token: String): Option[User] = {
    val decoded = decodeToken(token)
    if (decoded.isSuccess) {
      Some(decoded.get.getClaim[Usr].get.value)
    } else {
      None
    }
  }

  def validateRegistrationToken(token: String): Boolean = {
    DecodedJwt.validateEncodedJwt(
      token,
      secretKey,
      Algorithm.HS256,
      Set(Typ),
      Set(Uid),
    ).isSuccess
  }


  def createLoginToken(user: User): String = {
    val jwt = new DecodedJwt(Seq(Alg(Algorithm.HS256), Typ("JWT")), Seq(Usr(user)))
    jwt.encodedAndSigned(secretKey)
  }


  def createRegistrationToken(id: Int): String = {
    val jwt = new DecodedJwt(Seq(Alg(Algorithm.HS256), Typ("JWT")), Seq(Uid(id)))
    jwt.encodedAndSigned(secretKey)
  }


  case class Uid(value: Long) extends ClaimValue {
    override val field: ClaimField = Uid
    override val jsValue: JsValue = JsNumber(value)
  }

  object Uid extends (Long => Uid) with ClaimField {
    override def attemptApply(value: JsValue): Option[ClaimValue] =
      value.asOpt[Long].map(apply)

    override val name: String = "uid"
  }


  case class Usr(value: User) extends ClaimValue {
    override val field: ClaimField = Usr

    implicit def from[A](writes: OWrites[A]): OWritesOps[A] = new OWritesOps(writes)

    implicit val userWrites: OWrites[User] = Json.writes[User]
      .removeField("password")
      .removeField("profileImage")
    override val jsValue: JsValue = Json.toJson(value)
  }

  object Usr extends (User => Usr) with ClaimField {
    val userDefaults = User(None, "", "", None, None, None, None, None, None, None, Some(false), Some(false))
    implicit val userReads = new ReadsWithDefaults(userDefaults)(Json.format[User])

    override def attemptApply(value: JsValue): Option[Usr] = {
      value.asOpt[User].map(apply)
    }

    override val name: String = "usr"
  }


  def isLoggedIn: Directive1[User] =
    optionalHeaderValueByName("Authorization").flatMap {
      case Some(jwt) => validateLoginToken(jwt) match {
        case Some(user) => provide(user)
        case None => complete((StatusCodes.OK, "null"))
      }
      case _ => complete((StatusCodes.OK, "null"))
    }

  def authenticatedUser: Directive1[User] =
    optionalHeaderValueByName("Authorization").flatMap {
      case Some(jwt) => validateLoginToken(jwt) match {
        case Some(user) => provide(user)
        case None => complete(StatusCodes.Unauthorized)
      }
      case _ => complete(StatusCodes.Unauthorized)
    }

  def authenticatedAdmin: Directive1[User] =
    authenticatedUser.flatMap { user =>
      if (user.isAdmin.getOrElse(false)) provide(user) else complete(StatusCodes.Unauthorized)
    }

  def verifiedUser: Directive1[User] =
    authenticatedUser.flatMap { user =>
      if (user.verified.getOrElse(false)) provide(user) else complete(StatusCodes.Unauthorized)
    }

}
