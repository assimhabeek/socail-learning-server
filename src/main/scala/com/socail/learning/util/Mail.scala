package com.socail.learning.util

import javax.mail.internet.InternetAddress

import courier._
import Defaults._

import scala.concurrent.Future

object Mail {

  lazy val mailer: Mailer = Mailer("smtp.gmail.com", 587)
    .auth(true)
    .as("assimhabe@gmail.com", "assi2015")
    .startTtls(true)()
  val sender = new InternetAddress("socail@social.com", "NTIC Social Learning")

  def sendMail(to: InternetAddress, subject: String, content: Content): Future[Unit] = {
    val message = mailer(Envelope
      .from(sender)
      .to(to)
      .replyTo(sender)
      .subject(subject)
      .content(content))
    message
  }

}
