package com.socail.learning.domain

case class Category(
  id: Option[Int],
  title: String,
  description: String,
  icon: String,
) extends BasicEntity

