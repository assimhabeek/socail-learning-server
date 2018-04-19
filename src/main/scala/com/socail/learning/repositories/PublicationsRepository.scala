package com.socail.learning.repositories

import com.socail.learning.domain.{ Publication, User }
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PublicationsRepository(override val config: DatabaseConfig[JdbcProfile])
    extends BaseRepository[Publication](config) {

  import config.profile.api._

  val publicationsPaged = Compiled((d: ConstColumn[Long]) =>
    (publications join users on (_.userId === _.id)).sortBy(x => x._1.date.desc).drop(d).take(10))

  override def schema(): TableQuery[BasicRow[Publication]] =
    publications.asInstanceOf[TableQuery[BasicRow[Publication]]]

  def findByPage(page: Int): Future[Seq[(Publication, User)]] = {
    db.run(publicationsPaged(page * 10).result).recover {
      case e: Exception =>
        println(e.getMessage)
        Seq()
    }
  }

  override def delete(id: Int) = {
    val seq: DBIOAction[Unit, NoStream, Effect.Write] = DBIO.seq(
      comments.filter(_.publicationId === id).delete,
      attachments.filter(_.publicationId === id).delete,
      publications.filter(_.id === id).delete
    )
    db.run(seq).map(_ => true).recover {
      case e: Exception =>
        println(e.getMessage)
        false
    }
  }
}
