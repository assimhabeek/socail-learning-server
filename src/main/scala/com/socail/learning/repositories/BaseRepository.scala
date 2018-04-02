package com.socail.learning.repositories

import com.socail.learning.schema.SocialSchema

import scala.concurrent.ExecutionContext.Implicits.global
import slick.basic.DatabaseConfig
import slick.dbio.DBIOAction
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

abstract class BaseRepository[T](val config: DatabaseConfig[JdbcProfile])
    extends SocialSchema {

  import config.profile.api._

  def schema(): TableQuery[BasicRow[T]]

  def init(): Future[Unit] = db.run(DBIOAction.seq(schema().schema.create)).recover {
    case e: Exception =>
      println(e.getMessage)
  }

  def drop(): Future[Unit] = db.run(DBIOAction.seq(schema().schema.drop)).recover {
    case e: Exception =>
      println(e.getMessage)
  }

  def findById(id: Int): Future[Option[T]] = db.run(schema().filter(_.id === id).result.headOption).recover {
    case e: Exception =>
      println(e.getMessage)
      None
  }

  def findAll(): Future[Seq[T]] = db.run(schema().result).recover {
    case e: Exception =>
      println(e.getMessage)
      Seq()
  }

  def insert(item: T): Future[Option[Int]] =
    db.run(schema() returning schema().map(_.id) += item).recover {
      case e: Exception =>
        println(e.getMessage)
        Some(-1)
    }

  def update(id: Int, item: T): Future[Int] = db.run(schema().filter(_.id === id).update(item)).recover {
    case e: Exception =>
      println(e.getMessage)
      -1
  }

  def delete(id: Int): Future[Boolean] =
    db.run(schema().filter(_.id === id).delete).map(_ > 0).recover {
      case e: Exception =>
        println(e.getMessage)
        false
    }

  def count(): Future[Int] = {
    db.run(schema().length.result).recover {
      case e: Exception =>
        println(e.getMessage)
        -1
    }
  }
}
