package com.socail.learning.domain

case class User(
  id: Option[Int],
  username: String,
  var password: String,
  firstName: Option[String],
  lastName: Option[String],
  about: Option[String],
  email: Option[String],
  year: Option[Int],
  specialtyId: Option[Int],
  profileImage: Option[Array[Byte]],
  isAdmin: Option[Boolean],
  verified: Option[Boolean]
) extends BasicEntity

