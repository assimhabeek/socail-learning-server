package com.socail.learning.domain

import java.sql.Timestamp

case class Publication(
  id: Option[Int],
  title: String,
  description: Option[String],
  date: Timestamp,
  userId: Int,
  commentable: Option[Boolean],
  categoryId: Int,
  specialtyId: Option[Int],
  moduleId: Option[Int]
) extends Contribution
