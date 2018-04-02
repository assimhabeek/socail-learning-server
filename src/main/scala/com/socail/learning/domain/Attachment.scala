package com.socail.learning.domain

case class Attachment(
  id: Option[Int],
  name: String,
  link: String,
  publicationId: Int
)
