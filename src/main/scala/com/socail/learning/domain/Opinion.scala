package com.socail.learning.domain

case class Opinion(
  id: Option[Int],
  userId: Int,
  publicationId: Int,
  opinion: Int,
  description: Option[String]
) extends BasicEntity

object OpinionOptions {
  val OPTION_LIKED = 1
  val OPTION_DISLIKED = 2
  val OPTION_REPORTED = 3
}