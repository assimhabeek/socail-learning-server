package com.socail.learning.repositories

import scala.concurrent.ExecutionContext.Implicits.global
import com.socail.learning.domain.Attachment
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class AttachmentsRepository(override val config: DatabaseConfig[JdbcProfile])
    extends BaseRepository[Attachment](config) {

  import config.profile.api._

  override def schema(): TableQuery[BasicRow[Attachment]] =
    attachments.asInstanceOf[TableQuery[BasicRow[Attachment]]]

  def findWithPublication(id: Int): Future[Seq[Attachment]] = {
    val rs = attachments.filter(_.publicationId === id)
    db.run(rs.result).recover {
      case e: Exception =>
        println(e.getMessage)
        Seq()
    }
  }

}
