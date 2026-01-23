import scala.collection.Seq

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.7"

lazy val root = (project in file("."))
  .settings(
    name := "ProyectoPracticum",
    libraryDependencies ++= Seq(
      "io.reactivex" % "rxscala_2.13" % "0.27.0",
      "de.tu-darmstadt.stg" %% "rescala" % "0.35.0",

      "org.gnieh" %% "fs2-data-csv" % "1.11.1",
      "org.gnieh" %% "fs2-data-csv-generic" % "1.11.1",

      "co.fs2" %% "fs2-core" % "3.12.2",
      "co.fs2" %% "fs2-io" % "3.12.2",

      "io.circe" %% "circe-core" % "0.14.6",
      "io.circe" %% "circe-generic" % "0.14.6",
      "io.circe" %% "circe-parser" % "0.14.6",

      // DOOBIE 1.x (CORRECTO)
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC11",
      "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC11",

      // MySQL
      "com.mysql" % "mysql-connector-j" % "9.1.0",

      // Config + logging
      "com.typesafe" % "config" % "1.4.2",
      "org.slf4j" % "slf4j-simple" % "2.0.16"
    )
  )
