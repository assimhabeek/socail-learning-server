package com.socail.learning.repositorie

import com.socail.learning.domain.Domain.{ Db, AddressesTable }
import slick.basic.DatabaseConfig
import slick.dbio.DBIOAction
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class AddressesRepository(val config: DatabaseConfig[JdbcProfile])
    extends Db with AddressesTable {
  import config.profile.api._
  import scala.concurrent.ExecutionContext.Implicits.global

  def init(): Future[Unit] = db.run(DBIOAction.seq(addresses.schema.create))
  def drop(): Future[Unit] = db.run(DBIOAction.seq(addresses.schema.drop))

}
