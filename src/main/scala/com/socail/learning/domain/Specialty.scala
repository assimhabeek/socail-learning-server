package com.socail.learning.domain

case class Specialty(
  id: Option[Int],
  abb: String,
  name: String,
  from: Int,
  to: Int
) extends BasicEntity(id)

