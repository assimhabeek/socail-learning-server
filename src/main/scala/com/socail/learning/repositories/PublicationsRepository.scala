package com.socail.learning.repositories

import com.socail.learning.domain.{ Publication, User }
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PublicationsRepository(override val config: DatabaseConfig[JdbcProfile])
    extends BaseRepository[Publication](config) {

  import config.profile.api._

  val publicationsPaged = Compiled((d: ConstColumn[Long]) => publications.sortBy(_.date.desc).drop(d).take(10) join users on (_.userId === _.id))

  override def schema(): TableQuery[BasicRow[Publication]] =
    publications.asInstanceOf[TableQuery[BasicRow[Publication]]]

  def findByPage(page: Int): Future[Seq[(Publication, User)]] = {
    db.run(publicationsPaged(page * 10).result).recover {
      case e: Exception =>
        println(e.getMessage)
        Seq()
    }
  }

}
