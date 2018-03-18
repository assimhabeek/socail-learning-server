package com.socail.learning.repositories

import com.socail.learning.domain.BasicEntity

import scala.concurrent.ExecutionContext.Implicits.global
import com.socail.learning.schema.BasicSchema
import slick.basic.DatabaseConfig
import slick.dbio.DBIOAction
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

abstract class BaseRepository[T](val config: DatabaseConfig[JdbcProfile])
    extends BasicSchema[T] {

  import config.profile.api._

  def schema(): TableQuery[BasicRow]

  def init(): Future[Unit] = db.run(DBIOAction.seq(schema().schema.create))

  def drop(): Future[Unit] = db.run(DBIOAction.seq(schema().schema.drop))

  def findById(id: Int): Future[Option[T]] =
    db.run(schema().filter(_.id === id).result.headOption)

  def findAll(): Future[Seq[T]] = db.run(schema().result)

  def insert(item: T): Future[Option[Int]] = db.run(schema() returning schema().map(_.id) += item)

  def update(item: T): Future[Int] = db.run(schema().update(item))

  def delete(id: Int): Future[Boolean] =
    db.run(schema().filter(_.id === id).delete) map {
      _ > 0
    }

  def count(): Future[Int] = {
    db.run(schema().length.result)
  }
}
