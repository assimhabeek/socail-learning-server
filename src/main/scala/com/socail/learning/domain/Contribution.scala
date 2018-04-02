package com.socail.learning.domain

import java.sql.Timestamp

trait Contribution extends BasicEntity {
  val description: Option[String]
  val date: Timestamp
  val userId: Int
}
