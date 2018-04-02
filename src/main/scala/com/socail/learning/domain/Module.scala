package com.socail.learning.domain

case class Module(
  id: Option[Int],
  abb: String,
  name: Option[String],
  spcailtyId: Int,
  year: Int,
  semmster: Int
) extends BasicEntity

