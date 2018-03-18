package com.socail.learning.util

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{complete, optionalHeaderValueByName, provide}
import com.socail.learning.domain.User
import io.igl.jwt._
import play.api.libs.json.{JsNumber, JsValue}

import scala.util.Try

trait AuthenticationHandler {
  val secretKey = "@#TNf_*34ASsad3164$%#$FFDDSAF15365"

  def getDecodedRes(token: String): Try[Jwt] = {
    val res: Try[Jwt] = DecodedJwt.validateEncodedJwt(
      token,
      secretKey,
      Algorithm.HS256,
      Set(Typ),
      Set(Uid, Adm),
    )
    res
  }

  def validateToken(token: String): Option[(Long, Boolean)] = {
    val res = getDecodedRes(token)
    if (res.isSuccess) {
      val adminClaimValue: Long = res.get.getClaim[Adm].get.value
      val userIdClaimValue: Long = res.get.getClaim[Uid].get.value
      Some((userIdClaimValue, adminClaimValue == 1))
    } else {
      None
    }
  }


  def createTokenWith(user: User): String = {
    val isAdmin: Int = if (user.isAdmin.get) 1 else 0
    val jwt = new DecodedJwt(Seq(Alg(Algorithm.HS256), Typ("JWT")), Seq(Uid(user.id.get), Adm(isAdmin)))
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

  case class Adm(value: Long) extends ClaimValue {
    override val field: ClaimField = Adm
    override val jsValue: JsValue = JsNumber(value)
  }

  object Adm extends (Long => Adm) with ClaimField {
    override def attemptApply(value: JsValue): Option[ClaimValue] =
      value.asOpt[Long].map(apply)

    override val name: String = "adm"
  }

  private def authenticatedUser: Directive1[(Long, Boolean)] =
    optionalHeaderValueByName("Authorization").flatMap {
      case Some(jwt) => validateToken(jwt) match {
        case Some(user) => provide(user)
        case None => complete(StatusCodes.Unauthorized)
      }
      case _ => complete(StatusCodes.Unauthorized)
    }

  private def authenticatedAdmin: Directive1[(Long, Boolean)] =
    authenticatedUser.flatMap { user =>
      if (user._2) provide(user) else complete(StatusCodes.Unauthorized)
    }

}
