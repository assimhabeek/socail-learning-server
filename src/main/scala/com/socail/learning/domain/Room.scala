package com.socail.learning.domain

import java.sql.Timestamp

case class Room(
                 id: Option[Int],
                 creator: Int,
                 firstPerson: Int,
                 creatorRead: Boolean,
                 firstPersonRead: Boolean,
               )