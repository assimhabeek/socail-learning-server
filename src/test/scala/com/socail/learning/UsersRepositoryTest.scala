package com.socail.learning

import com.socail.learning.domain.DomainConfig.DbConfiguration
import com.socail.learning.domain.User
import com.socail.learning.repositories.UsersRepository
import com.socail.learning.schema.UsersSchema
import org.mindrot.jbcrypt.BCrypt
import org.specs2.matcher.{ FutureMatchers, OptionMatchers }
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterEach

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration

class UsersRepositoryTest extends Specification with DbConfiguration
    with FutureMatchers
    with OptionMatchers
    with BeforeAfterEach
    with UsersSchema {

  import config.profile.api._

  sequential

  val repo = new UsersRepository(config)

  def before: Future[Unit] = {
    repo.init()
  }

  def after(): Unit = {
    repo.drop()
  }

  /*
  "User should be inserted successfully" >> {
    val user: User = User(None, "assimhabe", "abcd", Some("firstname"), Some("lastname"), Some("assimhabe@gmail.com"), Some(5), Some(2), Some(false))
    Await.result(repo.insert(user), Duration.Inf) must beSome(1)
  }

  "should get all users" >> {
    val setup = DBIO.seq(
      users ++= Seq(
        User(None, "test-01", "abcd", Some("firstname"), Some("lastname"), Some("assimhabe@gmail.com"), Some(5), Some(2), Some(false)),
        User(None, "test-02", "abcd", Some("firstname"), Some("lastname"), Some("assimha@gmail.com"), Some(5), Some(2), Some(false)),
        User(None, "test-03", "abcd", Some("firstname"), Some("lastname"), Some("assim@gmail.com"), Some(5), Some(2), Some(false)),
        User(None, "test-04", "abcd", Some("firstname"), Some("lastname"), Some("ass@gmail.com"), Some(5), Some(2), Some(false)),
      )
    )
    db.run(setup)
    Await.result(repo.findAll(), Duration.Inf).length must_== 4
  }

  "should authenticate user by returning Some" >> {
    val username = "test-01"
    val password = "abcd"
    db.run(users += User(None, username, BCrypt.hashpw(password, BCrypt.gensalt()), Some("firstname"), Some("lastname"), Some("assimhabe@gmail.com"), Some(5), Some(2), Some(false)))
    repo.findByUsernamePassword(username, password)
    val user = Await.result(repo.findByUsernamePassword(username, password), Duration.Inf)
    user must beSome((x: User) => x.username mustEqual username)
    user must beSome((x: User) => BCrypt.checkpw(password, x.password))
  }
*/

  "should refuse user by returning None" >> {
    val username = "test-01"
    val password = "abcd"
    db.run(users += User(None, username, BCrypt.hashpw(password, BCrypt.gensalt()), Some("firstname"), Some("lastname"), Some("assimhabe@gmail.com"), Some(5), Some(2), Some(false)))
    repo.findBy(username, password)
    val user = Await.result(repo.findBy(username, "fsdaf" + password), Duration.Inf)
    user must beNone
  }

}