package com.socail.learning.repositories

import javax.mail.internet.{ InternetAddress, MimeBodyPart }

import com.socail.learning.domain.{ OpinionOptions, User }
import com.socail.learning.schema.SocialSchema
import com.socail.learning.util.{ AuthenticationHandler, Mail }
import courier.{ Multipart, Text }
import org.mindrot.jbcrypt.BCrypt
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import spray.json
import spray.json._
import com.socail.learning.util.SLProtocal._

import scala.concurrent.Future
import scala.io.Source

class UsersRepository(override val config: DatabaseConfig[JdbcProfile])
    extends BaseRepository[User](config) with AuthenticationHandler {

  import config.profile.api._
  import scala.concurrent.ExecutionContext.Implicits.global

  override def schema(): TableQuery[BasicRow[User]] =
    users.asInstanceOf[TableQuery[BasicRow[User]]]

  override def insert(item: User): Future[Int] = {
    item.password = BCrypt.hashpw(item.password, BCrypt.gensalt())
    super.insert(item) map { x =>
      sendRegistrationEmail(x, item.email.getOrElse(""))
      x
    }
  }

  def findBy(username: String, password: String): Future[Option[User]] =
    db.run(users.filter(x => x.username === username || x.email === username).result
      map (_.headOption.filter(x => BCrypt.checkpw(password, x.password))))

  def findByUsername(username: String): Future[Option[User]] =
    db.run(users.filter(_.username === username).result.headOption)

  def findByEmail(email: String): Future[Option[User]] =
    db.run(users.filter(_.email === email).result.headOption)

  def sendRegistrationEmail(id: Int, email: String): Future[Unit] = {
    val token = createRegistrationToken(id)
    val stream = getClass.getResourceAsStream("/ValidateEmailTemplate.html")
    val template: String = Source.fromInputStream(stream).getLines().mkString.replace("TOKEN", token)
    stream.close()
    val body = new MimeBodyPart()
    body.setContent(template, "text/html; charset=utf-8")
    Mail.sendMail(
      new InternetAddress(email),
      "NTIC-SL : Vérification de l'E-mail",
      Multipart().add(body)
    )
  }

  def findReported() = {
    val query = opinions.filter(_.opinion === OpinionOptions.OPTION_REPORTED).groupBy(_.publicationId)
    db.run(users.filter(user => {
      user.id in publications.filter(pub => {
        pub.id in query.map { case (id, row) => (id, row.length) }.map(_._1)
      }).map(_.userId)
    }).map(x => (x.id, x.lastName, x.firstName, publications.filter(pub => {
      pub.id in query.map { case (id, row) => (id, row.length) }.map(_._1)
    }).length)).result)
      .recover {
        case e: Exception =>
          println(e.getMessage)
          Seq()
      }
  }

  def validateAccount(id: Int): Future[Int] = {
    val query = for { c <- users if c.id === id } yield c.verified
    val action = query.update(Some(true))
    db.run(action)
  }

  def sendPasswordRecoveryEmail(id: Int, email: String): Future[Unit] = {
    val newPasssword = randomString(10)
    val stream = getClass.getResourceAsStream("/PasswordRecoveryTemplate.html")
    val template: String = Source.fromInputStream(stream).getLines().mkString.replace("NEW_PASSWORD", newPasssword)
    stream.close()
    changeUserPassword(id, newPasssword)
    val body = new MimeBodyPart()
    body.setContent(template, "text/html; charset=utf-8")
    Mail.sendMail(
      new InternetAddress(email),
      "NTIC-SL : Récupération de mot de passe",
      Multipart().add(body)
    )
  }

  def changeUserPassword(id: Int, password: String): Future[Int] = {
    val query = for { c <- users if c.id === id } yield c.password
    val action = query.update(BCrypt.hashpw(password, BCrypt.gensalt()))
    db.run(action)
  }

  private def randomString(len: Int): String = {
    val rand = new scala.util.Random(System.nanoTime)
    val sb = new StringBuilder(len)
    val ab = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    for (i <- 0 until len) {
      sb.append(ab(rand.nextInt(ab.length)))
    }
    sb.toString
  }

  def updateUserInfo(user: User): Future[Int] = {
    val query = users.filter(_.id === user.id.getOrElse(0))
      .map(c => (c.about, c.firstName, c.lastName, c.year, c.specialtyId, c.profileImage))
    db.run(query.update(user.about, user.firstName, user.lastName, user.year, user.specialtyId, user.profileImage))
  }

  def getEva(id: Int): Future[Int] = db.run(publications.filter(_.userId === id).length.result).recover {
    case e: Exception =>
      println(e.getMessage)
      0
  }

  def getWitEva(uid: Int): Future[JsObject] = {
    findById(uid) flatMap {
      case Some(us) => getEva(us.id.get).map(g => {
        JsObject(us.toJson.asJsObject.fields + ("eve" -> JsNumber(g)))
      })
      case None => Future {
        JsObject.empty
      }
    }
  }

  def filterUsers(filter: String, page: Int): Future[(Int, Seq[JsObject])] = {
    val query = users.filter(x => x.lastName.indexOf(filter) >= 0
      || x.firstName.indexOf(filter) >= 0
      || x.email.indexOf(filter) >= 0
      || x.username.indexOf(filter) >= 0)
    db.run(query.drop(page * 10).take(10).result.map(y => query.length.result.map(i => (i, y.map(x => JsObject("id" -> JsNumber(x.id.getOrElse(0)), "lastName" -> JsString(x.lastName.getOrElse("")), "firstName" -> JsString(x.firstName.getOrElse("")), "profileImage" -> JsString(x.profileImage.getOrElse(Array()).map(_.toChar).mkString.replaceAll("\"", ""))))))).flatten)
  }

}
