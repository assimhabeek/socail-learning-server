package com.socail.learning.repositories

import com.socail.learning.domain.{ Specialty, User }
import com.socail.learning.schema.{ SpecialtiesSchema, UsersSchema }
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class SpecialtiesRepository(override val config: DatabaseConfig[JdbcProfile]) extends BaseRepository[Specialty](config) with SpecialtiesSchema {

  import config.profile.api._

  import scala.concurrent.ExecutionContext.Implicits.global

  override def schema(): TableQuery[BasicRow] = {
    specialties.asInstanceOf[TableQuery[BasicRow]]
  }

}
