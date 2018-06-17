package com.socail.learning.routes

import java.io.File
import java.nio.file.{ Files, Path, Paths }

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{ get, _ }
import akka.http.scaladsl.server.directives.FileInfo
import com.socail.learning.util.{ AuthenticationHandler, JsonSupport }
import com.socail.learning.util.SLProtocal._
import com.typesafe.config.ConfigFactory
import spray.json._

object FileRoutes extends JsonSupport {

  def tempDestination(fileInfo: FileInfo): File =
    File.createTempFile("upload-", fileInfo.fileName)

  lazy val conf = ConfigFactory.load("application.conf")
  lazy val outputDir: String = conf.getString("uploadPath")
  lazy val uploadAddress = s"${conf.getString("protocol")}${conf.getString("host")}:${conf.getString("port")}/download/"

  lazy val routes =
    concat(
      path("upload") {
        post {
          storeUploadedFile("file", tempDestination) {
            case (metadata, file) =>
              val temp: Path = Files.move(Paths.get(file.getPath), Paths.get(outputDir + file.getName))
              complete(StatusCodes.OK, JsObject("url" -> JsString(if (temp != null) {
                uploadAddress + temp.getFileName.toString
              } else {
                uploadAddress + "not-found"
              })))
          }
        }
      },
      path("download" / Segment) { name =>
        get {
          getFromFile(outputDir + name)
        }
      }
    )

}

