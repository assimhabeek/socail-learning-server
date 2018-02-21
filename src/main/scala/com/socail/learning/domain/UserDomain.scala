package com.socail.learning.domain

import com.socail.learning.domain.DomainConfig.Db

object UserDomain {

  case class User(id: Option[Int], email: String,
                  firstName: Option[String], lastName: Option[String])

  trait UsersTable {
    this: Db =>
    import config.profile.api._
    class UserRow(tag: Tag) extends Table[User](tag, "USERS") {
      def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
      def email = column[String]("EMAIL", O.Length(512))
      def firstName = column[Option[String]]("FIRST_NAME", O.Length(64))
      def lastName = column[Option[String]]("LAST_NAME", O.Length(64))
      def emailIndex = index("EMAIL_IDX", email, unique = true)
      def * = (id.?, email, firstName, lastName) <> (User.tupled, User.unapply)
    }
    val users = TableQuery[UserRow]
  }

}
