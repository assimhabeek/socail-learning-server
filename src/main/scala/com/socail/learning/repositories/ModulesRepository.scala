package com.socail.learning.repositories

import com.socail.learning.domain.Module
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ModulesRepository(override val config: DatabaseConfig[JdbcProfile])
    extends BaseRepository[Module](config) {

  import config.profile.api._

  override def schema(): TableQuery[BasicRow[Module]] =
    modules.asInstanceOf[TableQuery[BasicRow[Module]]]

  def findBySpecialty(sp: Int): Future[Seq[Module]] =
    db.run(modules.filter(_.specialtyId === sp).result).recover {
      case e: Exception =>
        println(e.getMessage)
        Seq()
    }

}
