package com.socail.learning.domain

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

object Domain {

  trait DbConfiguration {
    lazy val config = DatabaseConfig.forConfig[JdbcProfile]("db")
  }

  trait Db {
    val config: DatabaseConfig[JdbcProfile]
    val db: JdbcProfile#Backend#Database = config.db
  }

  case class User(id: Option[Int], email: String,
    firstName: Option[String], lastName: Option[String])

  case class Address(id: Option[Int], userId: Int,
    addressLine: String, city: String, postalCode: String)

  trait UsersTable {
    this: Db =>

    import config.profile.api._

    class Users(tag: Tag) extends Table[User](tag, "USERS") {
      def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

      def email = column[String]("EMAIL", O.Length(512))

      def firstName = column[Option[String]]("FIRST_NAME", O.Length(64))

      def lastName = column[Option[String]]("LAST_NAME", O.Length(64))

      def emailIndex = index("EMAIL_IDX", email, unique = true)

      def * = (id.?, email, firstName, lastName) <> (User.tupled, User.unapply)
    }

    val users = TableQuery[Users]
  }

  trait AddressesTable extends UsersTable {
    this: Db =>

    import config.driver.api._

    class Addresses(tag: Tag) extends Table[Address](tag, "ADDRESSES") {
      // Columns
      def id = column[Int]("ADDRESS_ID", O.PrimaryKey, O.AutoInc)

      def addressLine = column[String]("ADDRESS_LINE")

      def city = column[String]("CITY")

      def postalCode = column[String]("POSTAL_CODE")

      // ForeignKey
      def userId = column[Int]("USER_ID")

      def userFk = foreignKey("USER_FK", userId, users)(_.id, ForeignKeyAction.Restrict, ForeignKeyAction.Cascade)

      def * = (id.?, userId, addressLine, city, postalCode) <>
        (Address.tupled, Address.unapply)
    }

    val addresses = TableQuery[Addresses]
  }

}
