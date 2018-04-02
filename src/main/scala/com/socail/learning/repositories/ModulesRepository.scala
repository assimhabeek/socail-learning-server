package com.socail.learning.repositories

import com.socail.learning.domain.Module
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

class ModulesRepository(override val config: DatabaseConfig[JdbcProfile])
    extends BaseRepository[Module](config) {

  import config.profile.api._

  override def schema(): TableQuery[BasicRow[Module]] =
    modules.asInstanceOf[TableQuery[BasicRow[Module]]]

}
