package com.socail.learning.repositories

import com.socail.learning.domain.Friend
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FriendsRepository(override val config: DatabaseConfig[JdbcProfile])
    extends BaseRepository[Friend](config) {

  import config.profile.api._

  override def schema(): TableQuery[BasicRow[Friend]] =
    friends.asInstanceOf[TableQuery[BasicRow[Friend]]]

  def findFriends(id: Int): Future[Seq[Friend]] = {
    db.run(friends.filter(x => x.senderId === id || x.receiverId === id).result)
      .recover {
        case e: Exception =>
          println(e.getMessage)
          Seq()
      }

  }

}
