package com.socail.learning.repositories

import com.socail.learning.domain.{ Opinion, OpinionOptions }
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import com.socail.learning.util.JsonSupport
import com.socail.learning.util.SLProtocal._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class OpinionsRepository(override val config: DatabaseConfig[JdbcProfile])

    extends BaseRepository[Opinion](config) {

  import config.profile.api._

  override def schema(): TableQuery[BasicRow[Opinion]] =
    opinions.asInstanceOf[TableQuery[BasicRow[Opinion]]]

  def findByUserAndPublication(userId: Int, publicationId: Int): Future[Option[Opinion]] = {
    db.run(opinions.filter(x => x.userId === userId && x.publicationId === publicationId).result.headOption)
      .recover {
        case ex: Exception =>
          println(ex.getMessage)
          None
      }
  }

  def insertOrUpdate(item: Opinion): Future[(Int, Int)] = {
    val query = opinions.filter(x => x.userId === item.userId && x.publicationId === item.publicationId)
    val reQuery = opinions.filter(x => x.publicationId === item.publicationId)

    db.run(
      query.exists.result.flatMap {
      case true => query.map(x => (x.opinion, x.description)).update((item.opinion, item.description))
      case false => opinions returning opinions.map(_.id.get) += item
    }.flatMap { y =>
      (
        reQuery.filter(_.opinion === OpinionOptions.OPTION_LIKED).length,
        reQuery.filter(_.opinion === OpinionOptions.OPTION_DISLIKED).length
      ).result
    }.transactionally
    )
  }

}
