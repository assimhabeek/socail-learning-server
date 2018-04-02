package com.socail.learning.domain

import java.sql.Timestamp

case class Comment(
  id: Option[Int],
  description: Option[String],
  date: Timestamp,
  userId: Int,
  publicationId: Int,
  points: Option[Int],
  bestAnswer: Option[Boolean]
) extends Contribution
