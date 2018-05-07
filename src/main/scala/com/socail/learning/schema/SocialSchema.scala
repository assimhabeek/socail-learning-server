package com.socail.learning.schema

import java.sql.Timestamp

import com.socail.learning.domain.DomainConfig.Db
import com.socail.learning.domain._

trait SocialSchema extends Db {

  import config.profile.api._

  abstract class BasicRow[T](tag: Tag, name: String) extends Table[T](tag, name) {
    def id = column[Option[Int]]("ID", O.PrimaryKey, O.AutoInc)
  }

  class SpecialtyRow(tag: Tag) extends BasicRow[Specialty](tag, "SPECIALTIES") {
    def abb = column[String]("ABB", O.Length(45))

    def name = column[String]("NAME", O.Length(150))

    def from = column[Int]("FROM")

    def to = column[Int]("TO")

    def abbIndex = index("ABB_IDX", abb, unique = true)

    def * = (id, abb, name, from, to) <> (Specialty.tupled, Specialty.unapply)

  }

  val specialties = TableQuery[SpecialtyRow]

  class UserRow(tag: Tag) extends BasicRow[User](tag, "USERS") {

    def username = column[String]("USERNAME", O.Length(45))

    def password = column[String]("PASSWORD", O.Length(300))

    def firstName = column[Option[String]]("FIRST_NAME", O.Length(45))

    def lastName = column[Option[String]]("LAST_NAME", O.Length(45))

    def about = column[Option[String]]("ABOUT", O.Length(300))

    def email = column[Option[String]]("EMAIL", O.Length(45))

    def year = column[Option[Int]]("YEAR")

    def specialtyId = column[Option[Int]]("SPECIALTY_ID")

    def profileImage = column[Option[Array[Byte]]]("PROFILE_IMAGE")

    def isAdmin = column[Option[Boolean]]("IS_ADMIN", O.Default(Some(false)))

    def verified = column[Option[Boolean]]("VERIFIED", O.Default(Some(false)))

    def usernameIndex = index("USERNAME_IDX", username, unique = true)

    def emailIndex = index("EMAIL_IDX", email, unique = true)

    def specialty = foreignKey("SPE_FK", specialtyId, specialties)(_.id)

    def * = (id, username, password, firstName, lastName, about, email, year, specialtyId, profileImage, isAdmin, verified) <> (User.tupled, User.unapply)

  }

  val users: TableQuery[UserRow] = TableQuery[UserRow]

  class CategoryRow(tag: Tag) extends BasicRow[Category](tag, "CATEGORIES") {

    def title = column[String]("TITLE", O.Length(150))

    def description = column[String]("DESCRIPTION", O.Length(150))

    def icon = column[String]("ICON", O.Length(150))

    def * = (id, title, description, icon) <> (Category.tupled, Category.unapply)

  }

  val categories = TableQuery[CategoryRow]

  class ModuleRow(tag: Tag) extends BasicRow[Module](tag, "MODULES") {

    def abb = column[String]("ABB", O.Length(45))

    def name = column[Option[String]]("NAME", O.Length(150))

    def specialtyId = column[Int]("SPECIALTY_ID")

    def year = column[Int]("YEAR")

    def semmster = column[Int]("SEMMSTER")

    def specialty = foreignKey("MOD_SPE_FK", specialtyId, specialties)(_.id.get)

    def * = (id, abb, name, specialtyId, year, semmster) <> (Module.tupled, Module.unapply)

  }

  val modules = TableQuery[ModuleRow]

  class PublicationRow(tag: Tag) extends BasicRow[Publication](tag, "PUBLICATION") {

    def description = column[Option[String]]("DESCRIPTION")

    def date = column[Timestamp]("CON_DATE")

    def userId = column[Int]("USER_ID")

    def title = column[String]("TITLE")

    def commentable = column[Option[Boolean]]("COMMENTABLE")

    def categorieId = column[Int]("CATEGORIE_ID")

    def spcialtyId = column[Option[Int]]("SPCIALTY_ID")

    def moduleId = column[Option[Int]]("MODULE_ID")

    def user = foreignKey("USE_PUB_FK", userId, users)(_.id.get)

    def category = foreignKey("CAT_PUB_FK", categorieId, categories)(_.id.get)

    def spcailty = foreignKey("SPC_PUB_FK", spcialtyId, specialties)(_.id.get)

    def module = foreignKey("MOD_PUB_FK", moduleId, modules)(_.id.get)

    def * = (id, title, description, date, userId, commentable, categorieId, spcialtyId, moduleId) <> (Publication.tupled, Publication.unapply)

  }

  val publications: TableQuery[PublicationRow] = TableQuery[PublicationRow]

  class OpinionRow(tag: Tag) extends BasicRow[Opinion](tag, "OPINION") {

    def userId = column[Int]("USER_ID")

    def publicationId = column[Int]("PUBLICATION_ID")

    def opinion = column[Int]("OPINION")

    def description = column[Option[String]]("DESCRIPTION")

    def publication = foreignKey("OPINION_PUB_FK", publicationId, publications)(_.id.get)

    def user = foreignKey("OPINION_USER_FK", userId, users)(_.id.get, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.Cascade)

    def unique = index("publication_user", (userId, publicationId), unique = true)

    def * = (id, userId, publicationId, opinion, description) <> (Opinion.tupled, Opinion.unapply)

  }

  val opinions: TableQuery[OpinionRow] = TableQuery[OpinionRow]

  class FriendRow(tag: Tag) extends BasicRow[Friend](tag, "FRIEND") {

    def senderId = column[Int]("SENDER_ID")

    def receiverId = column[Int]("RECEIVER_ID")

    def state = column[Int]("STATE")

    def sender = foreignKey("SENDER_USER_FK", senderId, users)(_.id.get)

    def receiver = foreignKey("RECEIVER_USER_FK", receiverId, users)(_.id.get)

    def unique = index("sender_reciver", (senderId, receiverId), unique = true)

    def * = (id, senderId, receiverId, state) <> (Friend.tupled, Friend.unapply)

  }

  val friends: TableQuery[FriendRow] = TableQuery[FriendRow]

  class AttachmentRow(tag: Tag) extends BasicRow[Attachment](tag, "ATTACHMENTS") {

    def name = column[String]("NAME", O.Length(150))

    def link = column[String]("link")

    def publicationId = column[Int]("PUBLICATION_ID")

    def publication = foreignKey("ATT_PUB_FK", publicationId, publications)(_.id.get, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.Cascade)

    def * = (id, name, link, publicationId) <> (Attachment.tupled, Attachment.unapply)

  }

  val attachments = TableQuery[AttachmentRow]

  class CommentRow(tag: Tag) extends BasicRow[Comment](tag, "COMMENT") {

    def description = column[Option[String]]("DESCRIPTION", O.Length(45))

    def date = column[Timestamp]("CON_DATE")

    def userId = column[Int]("USER_ID")

    def publicationId = column[Int]("PUBLICATION_ID")

    def bestAnswer = column[Option[Boolean]]("BEST_ANSWER")

    def user = foreignKey("USE_COM_FK", userId, users)(_.id.get)

    def publication = foreignKey("PUB_COM_FK", publicationId, publications)(_.id.get, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.Cascade)

    def * = (id, description, date, userId, publicationId, bestAnswer) <> (Comment.tupled, Comment.unapply)

  }

  val comments: TableQuery[CommentRow] = TableQuery[CommentRow]

}
