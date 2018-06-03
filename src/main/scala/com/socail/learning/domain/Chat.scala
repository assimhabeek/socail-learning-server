package com.socail.learning.domain

import java.sql.Timestamp

case class Chat(
  id: Option[Int],
  message: String,
  sender: Int,
  messageDate: Timestamp,
  roomId: Int
)