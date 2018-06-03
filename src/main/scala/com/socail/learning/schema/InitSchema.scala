package com.socail.learning.schema

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import com.socail.learning.domain.User
import com.socail.learning.domain.Category
import com.socail.learning.domain.Specialty
import com.socail.learning.domain.Module

import scala.concurrent.Future

class InitSchema(val config: DatabaseConfig[JdbcProfile]) extends SocialSchema {

  import config.profile.api._

  def init(): Future[Unit] = {
    val setup = DBIO.seq(

      (
      userRooms.schema ++
      categories.schema ++
      specialties.schema ++
      users.schema ++
      modules.schema ++
      publications.schema ++
      opinions.schema ++
      comments.schema ++
      attachments.schema ++
      rooms.schema ++
      chats.schema ++
      friends.schema
    ).create,

      categories ++= Seq(
        Category(Some(1), "SHARE", "SHARE_DESCRIPTION"),
        Category(Some(2), "QUESTIONS", "QUESTIONS_DESCRIPTION"),
        Category(Some(3), "MEMORIES", "MEMORIES_DESCRIPTION"),
        Category(Some(4), "SCIENTIFIC_CLUBS", "SCIENTIFIC_CLUBS_DESCRIPTION")
      ),

      specialties ++= Seq(
        Specialty(Some(1), "TI", "Technology de de l‟Information", 3, 3),
        Specialty(Some(2), "SCI", "Sciences de l’Informatique", 3, 3),
        Specialty(Some(3), "SI", "Systèmes Informatiques", 3, 3),
        Specialty(Some(4), "GL", "Génie Logiciel", 3, 5),
        Specialty(Some(5), "SITW", "Systèmes d’Information et Technologies Web", 4, 5),
        Specialty(Some(6), "RSD", "Réseaux et Systèmes Distribués", 4, 5),
        Specialty(Some(7), "STIC", "Sciences et Technologies de l‟Information et de la Communication", 4, 5),
        Specialty(Some(8), "MI", "Tronc commun math et informatique", 1, 2)
      ),

      users ++= Seq(
        User(
          Some(1),
          "root",
          "$2a$04$xfZsvLi/xjIK1n2jhdRUWOUcxefkrp4KpP0OG6emydtlSanwKmir6",
          Some("assim"),
          Some("habeek"),
          Some("just a small description about me"),
          Some("assimahabe@gmail.com"), Some(3), Some(1), None, Some(true), Some(true)
        ),
        User(
          Some(2),
          "nabil",
          "$2a$04$Ub5EGWFz0/RlqrVzoClgnuVHzJ1wEOVBdf/TXqi/CsOTab5RrQS9u",
          Some("nabil"),
          Some("brighet"),
          Some("a simple description"),
          Some("nabil@gmail.com"), Some(3), Some(1), None, Some(true), Some(true)
        ),
        User(
          Some(3),
          "test-01",
          "$2a$04$Ub5EGWFz0/RlqrVzoClgnuVHzJ1wEOVBdf/TXqi/CsOTab5RrQS9u",
          Some("test-01"),
          Some("test-01"),
          Some("a simple description"),
          Some("test-01@gmail.com"), Some(3), Some(1), None, Some(false), Some(true)
        ),
        User(
          Some(4),
          "test-02",
          "$2a$04$Ub5EGWFz0/RlqrVzoClgnuVHzJ1wEOVBdf/TXqi/CsOTab5RrQS9u",
          Some("test-02"),
          Some("test-02"),
          Some("a simple description"),
          Some("test-02@gmail.com"), Some(3), Some(1), None, Some(false), Some(true)
        ),
        User(
          Some(5),
          "test-03",
          "$2a$04$Ub5EGWFz0/RlqrVzoClgnuVHzJ1wEOVBdf/TXqi/CsOTab5RrQS9u",
          Some("test-03"),
          Some("test-03"),
          Some("a simple description"),
          Some("test-03@gmail.com"), Some(3), Some(1), None, Some(false), Some(true)
        ),
        User(
          Some(6),
          "test-04",
          "$2a$04$Ub5EGWFz0/RlqrVzoClgnuVHzJ1wEOVBdf/TXqi/CsOTab5RrQS9u",
          Some("test-04"),
          Some("test-04"),
          Some("a simple description"),
          Some("test-04@gmail.com"), Some(3), Some(1), None, Some(false), Some(true)
        ),
        User(
          Some(7),
          "test-05",
          "$2a$04$Ub5EGWFz0/RlqrVzoClgnuVHzJ1wEOVBdf/TXqi/CsOTab5RrQS9u",
          Some("test-05"),
          Some("test-05"),
          Some("a simple description"),
          Some("test-05@gmail.com"), Some(3), Some(1), None, Some(false), Some(true)
        ),
        User(
          Some(8),
          "test-06",
          "$2a$04$Ub5EGWFz0/RlqrVzoClgnuVHzJ1wEOVBdf/TXqi/CsOTab5RrQS9u",
          Some("test-06"),
          Some("test-06"),
          Some("a simple description"),
          Some("test-06@gmail.com"), Some(3), Some(1), None, Some(false), Some(true)
        ),
        User(
          Some(9),
          "test-07",
          "$2a$04$Ub5EGWFz0/RlqrVzoClgnuVHzJ1wEOVBdf/TXqi/CsOTab5RrQS9u",
          Some("test-07"),
          Some("test-07"),
          Some("a simple description"),
          Some("test-07@gmail.com"), Some(3), Some(1), None, Some(false), Some(true)
        ), User(
          Some(10),
          "test-08",
          "$2a$04$Ub5EGWFz0/RlqrVzoClgnuVHzJ1wEOVBdf/TXqi/CsOTab5RrQS9u",
          Some("test-08"),
          Some("test-08"),
          Some("a simple description"),
          Some("test-08@gmail.com"), Some(3), Some(1), None, Some(false), Some(true)
        ), User(
          Some(11),
          "test-09",
          "$2a$04$Ub5EGWFz0/RlqrVzoClgnuVHzJ1wEOVBdf/TXqi/CsOTab5RrQS9u",
          Some("test-09"),
          Some("test-09"),
          Some("a simple description"),
          Some("test-09@gmail.com"), Some(3), Some(1), None, Some(false), Some(true)
        ), User(
          Some(12),
          "test-10",
          "$2a$04$Ub5EGWFz0/RlqrVzoClgnuVHzJ1wEOVBdf/TXqi/CsOTab5RrQS9u",
          Some("test-10"),
          Some("test-10"),
          Some("a simple description"),
          Some("test-10@gmail.com"), Some(3), Some(1), None, Some(false), Some(true)
        ), User(
          Some(13),
          "test-11",
          "$2a$04$Ub5EGWFz0/RlqrVzoClgnuVHzJ1wEOVBdf/TXqi/CsOTab5RrQS9u",
          Some("test-11"),
          Some("test-11"),
          Some("a simple description"),
          Some("test-11@gmail.com"), Some(3), Some(1), None, Some(false), Some(true)
        ), User(
          Some(14),
          "test-12",
          "$2a$04$Ub5EGWFz0/RlqrVzoClgnuVHzJ1wEOVBdf/TXqi/CsOTab5RrQS9u",
          Some("test-12"),
          Some("test-12"),
          Some("a simple description"),
          Some("test-12@gmail.com"), Some(3), Some(1), None, Some(false), Some(true)
        )

      ),

      modules ++= Seq(
        Module(Some(1), "DAW", Some("Développement d'applications web"), 1, 3, 1),
        Module(Some(2), "DAM", Some("Développement d'applications mobile"), 1, 3, 1),
        Module(Some(3), "BDM", None, 1, 3, 1),
        Module(Some(4), "IHM", None, 1, 3, 1),
        Module(Some(5), "IASR", None, 1, 3, 1),
        Module(Some(6), "OTAM", None, 1, 3, 1),
        Module(Some(7), "ACS", None, 1, 3, 1),
        Module(Some(8), "TEC", None, 1, 3, 1),
        Module(Some(9), "SSR", None, 1, 3, 2),
        Module(Some(10), "STO", None, 1, 3, 2),
        Module(Some(11), "GL-1", None, 8, 2, 2),
        Module(Some(12), "SE-1", None, 8, 2, 2),
        Module(Some(13), "AJ", None, 8, 2, 2),
        Module(Some(14), "DAW", None, 8, 2, 2),
        Module(Some(15), "BD", None, 8, 2, 2),
        Module(Some(16), "TG", None, 8, 2, 2),
        Module(Some(17), "AO", None, 8, 2, 1),
        Module(Some(18), "TL", None, 8, 2, 1),
        Module(Some(19), "ASD", None, 8, 2, 1),
        Module(Some(20), "POO", None, 8, 2, 1),
        Module(Some(21), "LM", None, 8, 2, 1)
      )

    )
    db.run(setup)
  }

}
