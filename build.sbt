lazy val akkaHttpVersion = "10.0.11"
lazy val akkaVersion = "2.5.8"


// sbt-assembly
lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.ti",
      scalaVersion := "2.12.4"
    )),
    name := "social-learning",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,

      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
      "org.scalatest" %% "scalatest" % "3.0.1" % Test,
      "com.typesafe.slick" %% "slick" % "3.2.1",
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.2.1",
      "mysql" % "mysql-connector-java" % "5.1.16",
      "com.h2database"  %  "h2"        % "1.4.177"
    )
  )

enablePlugins(JavaAppPackaging)