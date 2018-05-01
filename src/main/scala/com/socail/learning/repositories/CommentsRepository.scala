package com.socail.learning.repositories

import scala.concurrent.ExecutionContext.Implicits.global

import com.socail.learning.domain.{ Comment, User }
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class CommentsRepository(override val config: DatabaseConfig[JdbcProfile])
    extends BaseRepository[Comment](config) {

  import config.profile.api._

  override def schema(): TableQuery[BasicRow[Comment]] =
    comments.asInstanceOf[TableQuery[BasicRow[Comment]]]

  def findWithPublication(id: Int): Future[Seq[(Comment, User)]] = {
    val rs = comments.sortBy(_.date).filter(_.publicationId === id) join users on (_.userId === _.id)
    db.run(rs.result).recover {
      case e: Exception =>
        println(e.getMessage)
        Seq()
    }
  }

  def markAsBes(publicationId: Int, id: Int) {
    val unmarkedQuery = comments.filter { c => c.publicationId === publicationId && c.id =!= id }.map(_.bestAnswer)
    val markedQuery = comments.filter { c => c.publicationId === publicationId && c.id === id }.map(_.bestAnswer)
    db.run(unmarkedQuery.update(Some(false)).flatMap(_ => markedQuery.update(Some(true))).transactionally)
  }
}
