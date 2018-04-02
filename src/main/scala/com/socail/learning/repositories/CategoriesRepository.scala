package com.socail.learning.repositories

import com.socail.learning.domain.Category
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

class CategoriesRepository(override val config: DatabaseConfig[JdbcProfile])
    extends BaseRepository[Category](config) {

  import config.profile.api._

  override def schema(): TableQuery[BasicRow[Category]] =
    categories.asInstanceOf[TableQuery[BasicRow[Category]]]

}
