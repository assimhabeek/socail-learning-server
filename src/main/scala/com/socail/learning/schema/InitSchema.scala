package com.socail.learning.schema

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class InitSchema(val config: DatabaseConfig[JdbcProfile]) extends SocialSchema {

  import config.profile.api._

  def init(): Future[Unit] = {
    val setup = DBIO.seq(
      (specialties.schema ++
      users.schema ++
      categories.schema ++
      modules.schema ++
      publications.schema ++
      comments.schema ++
      attachments.schema).create
    )
    db.run(setup)
  }

}
