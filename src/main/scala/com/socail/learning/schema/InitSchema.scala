package com.socail.learning.schema

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class InitSchema(val config: DatabaseConfig[JdbcProfile]) extends SocialSchema {

  import config.profile.api._

  def init(): Future[Unit] = {
    val setup = DBIO.seq(

      (friends.schema ++
      opinions.schema ++
      publications.schema ++
      attachments.schema ++
      comments.schema ++
      specialties.schema ++
      users.schema ++
      categories.schema ++
      modules.schema).create

    )
    db.run(setup)
  }

}
