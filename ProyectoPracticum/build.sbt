ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.7"

val circeVersion = "0.14.10"

lazy val root = (project in file("."))
  .settings(
    name := "TrabajoGrupal",
    libraryDependencies ++= Seq(
      // Reactive y REScala
      "io.reactiveX" % "rxscala_2.13" % "0.27.0",
      "de.tu-darmstadt.stg" %% "rescala" % "0.35.0",

      // FS2 Data para CSV
      "org.gnieh" %% "fs2-data-csv" % "1.11.1",
      "org.gnieh" %% "fs2-data-csv-generic" % "1.11.1",

      // FS2 Core
      "co.fs2" %% "fs2-core" % "3.12.2",
      "co.fs2" %% "fs2-io" % "3.12.2",

      // Circe para JSON
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,

      // Cats Effect
      "org.typelevel" %% "cats-effect" % "3.5.7",

      // Doobie para Base de Datos
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC5",
      "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC5",

      // MySQL Connector
      "com.mysql" % "mysql-connector-j" % "9.1.0"
    ),

    // Opciones del compilador para Scala 3
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked"
    ),

    // Fork para evitar problemas con HikariCP
    fork := true
  )
