package com.socail.learning.domain

case class User(
  id: Option[Int],
  username: String,
  var password: String,
  firstName: Option[String],
  lastName: Option[String],
  email: Option[String],
  year: Option[Int],
  specialtyId: Option[Int],
  isAdmin: Option[Boolean]
) extends BasicEntity(id)

