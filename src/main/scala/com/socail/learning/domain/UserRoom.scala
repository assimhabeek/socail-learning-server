package com.socail.learning.domain

case class UserRoom(
  id: Option[Int],
  roomId: Int,
  userId: Int,
  read: Boolean
)