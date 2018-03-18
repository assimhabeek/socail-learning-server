package com.socail.learning.schema

import com.socail.learning.domain.User
import com.socail.learning.repositories.SpecialtiesRepository

trait UsersSchema extends BasicSchema[User] {

  import config.profile.api._

  lazy val specailties = new SpecialtiesRepository(config)

  class UserRow(tag: Tag) extends BasicRow(tag, "USERS") {

    def username = column[String]("USERNAME", O.Length(45))

    def password = column[String]("PASSWORD", O.Length(300))

    def firstName = column[Option[String]]("FIRST_NAME", O.Length(45))

    def lastName = column[Option[String]]("LAST_NAME", O.Length(45))

    def email = column[Option[String]]("EMAIL", O.Length(45))

    def year = column[Option[Int]]("YEAR")

    def specialtyId = column[Option[Int]]("SPECIALTY_ID")

    def isAdmin = column[Option[Boolean]]("IS_ADMIN")

    def usernameIndex = index("USERNAME_IDX", username, unique = true)

    def emailIndex = index("EMAIL_IDX", email, unique = true)

    def specialty = foreignKey("SPE_FK", specialtyId, specailties.specialties)(_.id)

    def * = {
      (id, username, password, firstName, lastName, email, year, specialtyId, isAdmin) <> (User.tupled, User.unapply)
    }

  }

  val users: TableQuery[UserRow] = TableQuery[UserRow]

}
