package com.socail.learning.domain

case class Friend(
  id: Option[Int],
  senderId: Int,
  receiverId: Int,
  state: Int
)