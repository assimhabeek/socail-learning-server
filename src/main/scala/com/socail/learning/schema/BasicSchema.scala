package com.socail.learning.schema

import com.socail.learning.domain.BasicEntity
import com.socail.learning.domain.DomainConfig.Db

trait BasicSchema[T] extends Db {

  import config.profile.api._

  abstract case class BasicRow(tag: Tag, override val tableName: String) extends Table[T](tag, tableName) {
    def id = column[Option[Int]]("ID", O.PrimaryKey, O.AutoInc)
  }

}
