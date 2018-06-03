package com.socail.learning.repositories

import java.sql.Timestamp
import java.sql.Date

import com.socail.learning.domain._
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.util.Try
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PublicationsRepository(override val config: DatabaseConfig[JdbcProfile])
  extends BaseRepository[Publication](config) {

  import config.profile.api._

  override def schema(): TableQuery[BasicRow[Publication]] =
    publications.asInstanceOf[TableQuery[BasicRow[Publication]]]

  def findByPage(page: Int,
                 textFilter: Option[String],
                 date: Option[String],
                 categoryId: Option[String],
                 specialtyId: Option[String],
                 moduleId: Option[String],
                 year: Option[String],
                ): Future[Seq[(Publication, User, Int, Int)]] = {

    db.run((publications join users on (_.userId === _.id) joinLeft specialties on (_._1.spcialtyId === _.id))
      .filter(qry => List(
        textFilter.map(s => qry._1._1.title.indexOf(s) >= 0 ||
          qry._1._1.description.getOrElse("(^_^)").indexOf(s) >= 0),
        date.map(s => qry._1._1.date <= new Timestamp(System.currentTimeMillis()) && qry._1._1.date >=
          Try(Timestamp.valueOf(s))
            .getOrElse(Timestamp.valueOf("1900-01-01 1:00:00"))),
        categoryId.map(i => qry._1._1.categorieId.getOrElse(0) === Try(i.toInt).getOrElse(0)),
        specialtyId.map(i => qry._1._1.spcialtyId.getOrElse(0) === Try(i.toInt).getOrElse(0)),
        moduleId.map(i => qry._1._1.moduleId.getOrElse(0) === Try(i.toInt).getOrElse(0)),
        year.map(i => qry._2.map(x => x.from <= Try(i.toInt).getOrElse(0) && x.to >= Try(i.toInt).getOrElse(0)).getOrElse(false))
      ).collect { case Some(cond) => cond }
        .reduceLeftOption(_ && _)
        .getOrElse(true: Rep[Boolean])
      ).sortBy(x => x._1._1.date.desc).drop(page * 10).take(10).map(x => {
      (x._1._1,
        x._1._2,
        opinions.filter(y => y.publicationId === x._1._1.id && y.opinion === OpinionOptions.OPTION_LIKED).length,
        opinions.filter(y => y.publicationId === x._1._1.id && y.opinion === OpinionOptions.OPTION_DISLIKED).length
      )
    }).result
    ).recover {
      case e: Exception =>
        println(e.getMessage)
        Seq()
    }
  }


  def findByUser(id: Int): Future[Seq[(Int, String)]] = {
    db.run(publications.filter(_.userId === id).map(x => (x.id.get, x.title)).result)
      .recover {
        case e: Exception =>
          println(e.getMessage)
          Seq()
      }
  }

  def findByPageAndFriend(page: Int, id: Int): Future[Seq[(Publication, User, Int, Int)]] = {
    db.run(publications.join(users).on((x, y) => x.userId === y.id && y.id =!= id).join(friends).on((x, y) => (x._2.id === y.senderId || x._2.id === y.receiverId) && (y.receiverId === id || y.senderId === id) && y.state === 3)
      .drop(page * 10).take(10).map(x => {
      (x._1._1, x._1._2,
        opinions.filter(y => y.publicationId === x._1._1.id && y.opinion === OpinionOptions.OPTION_LIKED).length,
        opinions.filter(y => y.publicationId === x._1._1.id && y.opinion === OpinionOptions.OPTION_DISLIKED).length)
    }).result
    ).recover {
      case e: Exception =>
        println(e.getMessage)
        Seq()
    }
  }

  def findByIdExtanded(id: Int): Future[Option[(Publication, User, Int, Int)]] = {
    db.run(publications.filter(_.id === id).join(users).on((x, y) => x.userId === y.id).map(x => {
      (x._1, x._2,
        opinions.filter(y => y.publicationId === id && y.opinion === OpinionOptions.OPTION_LIKED).length,
        opinions.filter(y => y.publicationId === id && y.opinion === OpinionOptions.OPTION_DISLIKED).length)
    }).result.headOption
    ).recover {
      case e: Exception =>
        println(e.getMessage)
        None
    }
  }

  def findReported() = {
    val query = opinions.filter(_.opinion === OpinionOptions.OPTION_REPORTED).groupBy(_.publicationId)
    db.run(publications.filter(pub => {
      pub.id in query.map { case (id, row) => (id, row.length) }.map(_._1)
    }).map(x => (x.id, x.title, opinions.filter(y => y.publicationId === x.id && y.opinion === OpinionOptions.OPTION_REPORTED).length)).result)
      .recover {
        case e: Exception =>
          println(e.getMessage)
          Seq()
      }
  }


  def deleteFrequentlyReported(): Unit = {
    db.run(publications.filter(pub => {
      pub.id in opinions.filter(_.opinion === OpinionOptions.OPTION_REPORTED).groupBy(_.publicationId).map { case (id, row) => (id, row.length) }.filter(_._2 >= 2).map(_._1)
    }).delete)
  }
}
