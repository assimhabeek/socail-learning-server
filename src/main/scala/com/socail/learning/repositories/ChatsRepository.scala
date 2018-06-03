package com.socail.learning.repositories

import com.socail.learning.domain.{ Chat, Room, User, UserRoom }
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import spray.json.{ JsNumber, JsObject, JsString }
import com.socail.learning.util.SLProtocal._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Success, Try }

class ChatsRepository(override val config: DatabaseConfig[JdbcProfile])
    extends BaseRepository[Chat](config) {

  import config.profile.api._

  override def schema(): TableQuery[BasicRow[Chat]] =
    chats.asInstanceOf[TableQuery[BasicRow[Chat]]]

  def createRoom(room: Room): Future[Int] = {
    val query = rooms.filter(x => ((x.creatorId === room.creator && x.firstPerson === room.firstPerson) ||
      (x.creatorId === room.firstPerson && x.firstPerson === room.creator)) && !x.id.in(userRooms.map(_.roomId)))
    db.run(
      query.exists.result.flatMap {
      case false => rooms returning rooms.map(_.id.get) += room
      case true => query.result.head.map(_.id.get)
    }.transactionally
    )
  }

  def addToRoom(room: Int, userId: Int): Future[Int] = {
    db.run(userRooms returning userRooms.map(_.id.get) += UserRoom(None, room, userId))
  }

  def removeFromRoom(room: Int, userId: Int): Future[Int] = {
    db.run(userRooms.filter(x => x.roomId === room && x.userId === userId).delete)
  }

  def isRegistredToRoom(room: Int, userId: Int): Future[Boolean] = {
    db.run((userRooms.filter(_.userId === userId).map(_.roomId)
      union rooms.filter(x => x.creatorId === userId || x.firstPerson === userId).map(_.id)).exists.result)
  }

  def getRoomsWithUsers(userId: Int): Future[Seq[JsObject]] = {
    val coll: Future[Seq[(Int, Future[Seq[JsObject]])]] = getUserRooms(userId).map(y => {
      y.map(z => (z.getOrElse(0), getRoomUsers(z.getOrElse(0), userId)))
    })
    coll.map(x =>
      Future.sequence(x.map(y => {
        y._2.map(i => {
          JsObject("room" -> JsNumber(y._1), "users" -> i.toJson)
        })
      }))).flatten
  }

  def getUserRooms(userId: Int): Future[Seq[Option[Int]]] = {
    val roomsOfCurrentUser = userRooms.filter(_.userId === userId).map(_.roomId) union
      rooms.filter(x => x.creatorId === userId || x.firstPerson === userId).map(_.id)
    db.run(roomsOfCurrentUser.result)
  }

  def getRoomUsers(roomId: Int, userId: Int): Future[Seq[JsObject]] = {
    db.run(users.filter(x => x.id =!= userId && (x.id.in(userRooms.filter(_.roomId === roomId).map(_.userId))
      || x.id.in(rooms.filter(y => y.id === roomId).map(_.creatorId))
      || x.id.in(rooms.filter(y => y.id === roomId).map(_.firstPerson)))).result.map(y => {
      y.map(x => JsObject("id" -> JsNumber(x.id.getOrElse(0)), "lastName" -> JsString(x.lastName.getOrElse("")), "firstName" -> JsString(x.firstName.getOrElse("")), "profileImage" -> JsString(x.profileImage.getOrElse(Array()).map(_.toChar).mkString.replaceAll("\"", ""))))
    }))
  }

  def getMessages(roomId: Int): Future[Seq[Chat]] = {
    db.run(chats.filter(_.roomId === roomId).sortBy(_.messageDate).result)
  }

}
