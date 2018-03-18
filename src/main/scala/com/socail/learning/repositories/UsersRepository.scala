package com.socail.learning.repositories

import javax.mail.internet.InternetAddress

import com.socail.learning.domain.User
import com.socail.learning.schema.UsersSchema
import com.socail.learning.util.Mail
import courier.Text
import org.mindrot.jbcrypt.BCrypt
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class UsersRepository(override val config: DatabaseConfig[JdbcProfile]) extends BaseRepository[User](config) with UsersSchema {

  import config.profile.api._
  import scala.concurrent.ExecutionContext.Implicits.global

  override def schema(): TableQuery[BasicRow] = {
    users.asInstanceOf[TableQuery[BasicRow]]
  }

  override def insert(item: User): Future[Option[Int]] = {
    item.password = BCrypt.hashpw(item.password, BCrypt.gensalt())
    super.insert(item) map { x =>
      sendRegistrationEmail(item, x)
      x
    }
  }

  def sendRegistrationEmail(user: User, id: Option[Int]): Unit = {
    Mail.sendMail(new InternetAddress(user.email.get),
      "NTIC S L email Verification",
      Text(s"your id is ${id.get}"))
  }

  def findBy(username: String, password: String): Future[Option[User]] =
    db.run(users.filter(_.username === username).result
      map (_.headOption.filter(x => BCrypt.checkpw(password, x.password))))

}
