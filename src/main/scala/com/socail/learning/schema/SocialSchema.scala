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

    def specialty = foreignKey("SPE_FK", specialtyId, specialties)(_.id, onDelete = ForeignKeyAction.SetNull)

    def * = (id, username, password, firstName, lastName, about, email, year, specialtyId, profileImage, isAdmin, verified) <> (User.tupled, User.unapply)

  }

  val users: TableQuery[UserRow] = TableQuery[UserRow]

  class CategoryRow(tag: Tag) extends BasicRow[Category](tag, "CATEGORIES") {

    def title = column[String]("TITLE", O.Length(150))

    def description = column[String]("DESCRIPTION", O.Length(150))

    def * = (id, title, description) <> (Category.tupled, Category.unapply)

  }

  val categories = TableQuery[CategoryRow]

  class ModuleRow(tag: Tag) extends BasicRow[Module](tag, "MODULES") {

    def abb = column[String]("ABB", O.Length(45))

    def name = column[Option[String]]("NAME", O.Length(150))

    def specialtyId = column[Option[Int]]("SPECIALTY_ID")

    def year = column[Int]("YEAR")

    def semmster = column[Int]("SEMMSTER")

    def specialty = foreignKey("MOD_SPE_FK", specialtyId, specialties)(_.id, onDelete = ForeignKeyAction.Cascade)

    def * = (id, abb, name, specialtyId.getOrElse(0), year, semmster) <> (Module.tupled, Module.unapply)

  }

  val modules = TableQuery[ModuleRow]

  class PublicationRow(tag: Tag) extends BasicRow[Publication](tag, "PUBLICATION") {

    def description = column[Option[String]]("DESCRIPTION")

    def date = column[Timestamp]("CON_DATE")

    def userId = column[Option[Int]]("USER_ID")

    def title = column[String]("TITLE")

    def commentable = column[Option[Boolean]]("COMMENTABLE")

    def categorieId = column[Option[Int]]("CATEGORIE_ID")

    def spcialtyId = column[Option[Int]]("SPCIALTY_ID")

    def moduleId = column[Option[Int]]("MODULE_ID")

    def user = foreignKey("USE_PUB_FK", userId, users)(_.id, onDelete = ForeignKeyAction.Cascade)

    def category = foreignKey("CAT_PUB_FK", categorieId, categories)(_.id, onDelete = ForeignKeyAction.SetNull)

    def spcailty = foreignKey("SPC_PUB_FK", spcialtyId, specialties)(_.id, onDelete = ForeignKeyAction.SetNull)

    def module = foreignKey("MOD_PUB_FK", moduleId, modules)(_.id, onDelete = ForeignKeyAction.SetNull)

    def * = (id, title, description, date, userId.getOrElse(0), commentable, categorieId.getOrElse(0), spcialtyId, moduleId) <> (Publication.tupled, Publication.unapply)

  }

  val publications: TableQuery[PublicationRow] = TableQuery[PublicationRow]

  class OpinionRow(tag: Tag) extends BasicRow[Opinion](tag, "OPINION") {

    def userId = column[Option[Int]]("USER_ID")

    def publicationId = column[Option[Int]]("PUBLICATION_ID")

    def opinion = column[Int]("OPINION")

    def description = column[Option[String]]("DESCRIPTION")

    def publication = foreignKey("OPINION_PUB_FK", publicationId, publications)(_.id, onDelete = ForeignKeyAction.Cascade)

    def user = foreignKey("OPINION_USER_FK", userId, users)(_.id, onDelete = ForeignKeyAction.Cascade)

    def unique = index("publication_user", (userId, publicationId), unique = true)

    def * = (id, userId.getOrElse(0), publicationId.getOrElse(0), opinion, description) <> (Opinion.tupled, Opinion.unapply)

  }

  val opinions: TableQuery[OpinionRow] = TableQuery[OpinionRow]

  class FriendRow(tag: Tag) extends BasicRow[Friend](tag, "FRIEND") {

    def senderId = column[Option[Int]]("SENDER_ID")

    def receiverId = column[Option[Int]]("RECEIVER_ID")

    def state = column[Int]("STATE")

    def sender = foreignKey("SENDER_USER_FK", senderId, users)(_.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.Cascade)

    def receiver = foreignKey("RECEIVER_USER_FK", receiverId, users)(_.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.Cascade)

    def unique = index("sender_reciver", (senderId, receiverId), unique = true)

    def * = (id, senderId.getOrElse(0), receiverId.getOrElse(0), state) <> (Friend.tupled, Friend.unapply)

  }

  val friends: TableQuery[FriendRow] = TableQuery[FriendRow]

  class RoomRow(tag: Tag) extends BasicRow[Room](tag, "ROOM") {

    def creatorId = column[Option[Int]]("CREATOR_ID")

    def firstPerson = column[Option[Int]]("FIRST_PERSON_ID")

    def creatorRead = column[Boolean]("creatorRead")

    def firstPersonRead = column[Boolean]("firstPersonRead")

    def sender = foreignKey("ROOM_USER_FK", creatorId, users)(_.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.Cascade)

    def * = (id, creatorId.getOrElse(0), firstPerson.getOrElse(0), creatorRead, firstPersonRead) <> (Room.tupled, Room.unapply)

  }

  val rooms = TableQuery[RoomRow]

  class ChatRow(tag: Tag) extends BasicRow[Chat](tag, "CHAT") {

    def message = column[String]("MESSAGE", O.Length(150))

    def messageDate = column[Timestamp]("MESSAGE_DATE")

    def senderId = column[Option[Int]]("SENDER_ID")

    def roomId = column[Option[Int]]("ROOM_ID")

    def sender = foreignKey("CHAT_USER_FK", senderId, users)(_.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.Cascade)

    def room = foreignKey("MESSAGE_USER_FK", roomId, rooms)(_.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.Cascade)

    def * = (id, message, senderId.getOrElse(0), messageDate, roomId.getOrElse(0)) <> (Chat.tupled, Chat.unapply)

  }

  val chats = TableQuery[ChatRow]

  class UserRoomRow(tag: Tag) extends BasicRow[UserRoom](tag, "USERROOMS") {

    def userId = column[Option[Int]]("USER_ID")

    def roomId = column[Option[Int]]("ROOM_ID")

    def read = column[Boolean]("read")

    def user = foreignKey("US_RO_FX", userId, users)(_.id)

    def room = foreignKey("RO_US_FK", roomId, rooms)(_.id)

    def * = (id, roomId.getOrElse(0), userId.getOrElse(0), read) <> (UserRoom.tupled, UserRoom.unapply)

  }

  val userRooms = TableQuery[UserRoomRow]

  class AttachmentRow(tag: Tag) extends BasicRow[Attachment](tag, "ATTACHMENTS") {

    def name = column[String]("NAME", O.Length(150))

    def link = column[String]("link")

    def publicationId = column[Option[Int]]("PUBLICATION_ID")

    def publication = foreignKey("ATT_PUB_FK", publicationId, publications)(_.id, onDelete = ForeignKeyAction.Cascade)

    def * = (id, name, link, publicationId.getOrElse(0)) <> (Attachment.tupled, Attachment.unapply)

  }

  val attachments = TableQuery[AttachmentRow]

  class CommentRow(tag: Tag) extends BasicRow[Comment](tag, "COMMENT") {

    def description = column[Option[String]]("DESCRIPTION", O.Length(45))

    def date = column[Timestamp]("CON_DATE")

    def userId = column[Option[Int]]("USER_ID")

    def publicationId = column[Option[Int]]("PUBLICATION_ID")

    def bestAnswer = column[Option[Boolean]]("BEST_ANSWER")

    def user = foreignKey("USE_COM_FK", userId, users)(_.id)

    def publication = foreignKey("PUB_COM_FK", publicationId, publications)(_.id, onDelete = ForeignKeyAction.Cascade)

    def * = (id, description, date, userId.getOrElse(0), publicationId.getOrElse(0), bestAnswer) <> (Comment.tupled, Comment.unapply)

  }

  val comments: TableQuery[CommentRow] = TableQuery[CommentRow]

}
