package com.socail.learning.domain

import java.sql.Timestamp

case class Chat(
  id: Option[Int],
  senderId: Int,
  receiverId: Int,
  message: Option[String],
  messageDate: Timestamp
)