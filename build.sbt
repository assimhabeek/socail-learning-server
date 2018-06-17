lazy val akkaHttpVersion = "10.1.1"
lazy val akkaVersion = "2.5.12"
parallelExecution in Test := false
// sbt-assembly

resolvers ++= Seq(
  Resolver.mavenLocal,
  Resolver.bintrayRepo("lightshed", "maven"),
  DefaultMavenRepository)

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.ti",
      scalaVersion := "2.12.6"
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
      "com.typesafe.slick" %% "slick" % "3.2.3",
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.2.1",
      "mysql" % "mysql-connector-java" % "5.1.45",
      "com.h2database" % "h2" % "1.4.177",
      "org.xerial" % "sqlite-jdbc" % "3.23.1",
      "com.microsoft.sqlserver" % "mssql-jdbc" % "6.5.3.jre10-preview",
      "org.hsqldb" % "hsqldb" % "2.4.1",
      "io.igl" %% "jwt" % "1.2.2",
      "org.mindrot" % "jbcrypt" % "0.4",
      "ch.lightshed" %% "courier" % "0.1.4",
      "com.oracle" % "oracle" % "10.2.0.2.0",
      "org.postgresql" % "postgresql" % "9.4-1200-jdbc41"
    )
  )

enablePlugins(JavaAppPackaging)

assemblyMergeStrategy in assembly := {
  case PathList(ps @ _*) if ps.last endsWith ".class" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}