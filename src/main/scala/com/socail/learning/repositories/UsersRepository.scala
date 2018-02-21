package com.socail.learning.repositories

import com.socail.learning.domain.DomainConfig.Db
import com.socail.learning.domain.UserDomain.{User, UsersTable}
import slick.basic.DatabaseConfig
import slick.dbio.DBIOAction
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class UsersRepository(val config: DatabaseConfig[JdbcProfile])
  extends Db with UsersTable {

  import config.profile.api._
  import scala.concurrent.ExecutionContext.Implicits.global

  def init(): Future[Unit] = db.run(DBIOAction.seq(users.schema.create))

  def drop(): Future[Unit] = db.run(DBIOAction.seq(users.schema.drop))

  def insert(user: User): Future[User] =
    db.run(users returning users.map(_.id) += user)
      .map(id => user.copy(id = Some(id)))

  def find(id: Int): Future[Option[User]] = db.run(users.filter(_.id === id).result.headOption)

  def findAll(): Future[Seq[User]] = db.run(users.result)

}
