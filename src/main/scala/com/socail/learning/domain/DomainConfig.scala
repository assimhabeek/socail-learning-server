package com.socail.learning.domain

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

object DomainConfig {

  trait DbConfiguration {
    lazy val config = DatabaseConfig.forConfig[JdbcProfile]("db")
  }

  trait Db {
    val config: DatabaseConfig[JdbcProfile]
    val db: JdbcProfile#Backend#Database = config.db
  }

}
