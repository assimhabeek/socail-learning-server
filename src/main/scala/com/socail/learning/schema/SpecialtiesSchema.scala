package com.socail.learning.schema

import com.socail.learning.domain.Specialty

trait SpecialtiesSchema extends BasicSchema[Specialty] {

  import config.profile.api._

  class SpecialtyRow(tag: Tag) extends BasicRow(tag, "SPECIALTIES") {

    def abb = column[String]("ABB", O.Length(45))

    def name = column[String]("NAME", O.Length(150))

    def from = column[Int]("FROM")

    def to = column[Int]("TO")

    def abbIndex = index("ABB_IDX", abb, unique = true)

    def * = {
      (id, abb, name, from, to) <> (Specialty.tupled, Specialty.unapply)
    }
  }

  val specialties = TableQuery[SpecialtyRow]

}

