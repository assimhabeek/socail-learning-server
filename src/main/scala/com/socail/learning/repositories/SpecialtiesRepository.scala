package com.socail.learning.repositories

import com.socail.learning.domain.{ Specialty, User }
import com.socail.learning.schema.SocialSchema
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SpecialtiesRepository(override val config: DatabaseConfig[JdbcProfile])
    extends BaseRepository[Specialty](config) {

  import config.profile.api._

  override def schema(): TableQuery[BasicRow[Specialty]] =
    specialties.asInstanceOf[TableQuery[BasicRow[Specialty]]]

  def findByYear(year: Int): Future[Seq[Specialty]] =
    db.run(specialties.filter(x => x.from <= year && x.to >= year).result).recover {
      case e: Exception =>
        println(e.getMessage)
        Seq()
    }

}
