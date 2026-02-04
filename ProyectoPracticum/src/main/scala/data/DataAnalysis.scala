package data

import cats.effect.*
import fs2.*
import utilities.*
import Stats.*
import java.time.LocalDate

object DataAnalysis {

  //  NUMÉRICAS
  def analyzeNumeric(
                      rows: Stream[IO, Map[String, String]],
                      column: String
                    ): IO[Unit] =
    rows
      .map(r => Cleaners.toDouble(r(column)))
      .unNone
      .compile
      .toList
      .map { values =>
        println(s"\nColumna numérica: $column")
        println(s"Cantidad: ${values.size}")
        println(s"Mínimo: ${min(values)}")
        println(s"Máximo: ${max(values)}")
        println(s"Promedio: ${avg(values)}")
      }

  //  TEXTO
  def analyzeText(
                   rows: Stream[IO, Map[String, String]],
                   column: String,
                   top: Int = 10
                 ): IO[Unit] =
    rows
      .map(r => Cleaners.cleanString(r(column)))
      .unNone
      .compile
      .toList
      .map { values =>
        println(s"\nColumna texto: $column")
        values
          .groupBy(identity)
          .view
          .mapValues(_.size)
          .toList
          .sortBy(-_._2)
          .take(top)
          .foreach { case (v, c) =>
            println(f"$v%-20s -> $c")
          }
      }

  //  FECHAS
  def analyzeDates(
                    rows: Stream[IO, Map[String, String]],
                    column: String
                  ): IO[Unit] =
    rows
      .map(r => Cleaners.cleanDate(r(column)))
      .unNone
      .compile
      .toList
      .map { dates =>
        println(s"\nColumna fecha: $column")
        println(s"Cantidad válida: ${dates.size}")
        println(s"Fecha mínima: ${dates.min}")
        println(s"Fecha máxima: ${dates.max}")
      }

  //  JSON (ARRAY REPARABLE)
  def analyzeJsonArray(
                        rows: Stream[IO, Map[String, String]],
                        column: String
                      ): IO[Unit] =
    rows
      .map(r => Cleaners.jsonArraySize(r(column)))
      .unNone
      .compile
      .toList
      .map { sizes =>
        println(s"\nColumna JSON: $column")
        println(s"Registros válidos: ${sizes.size}")
        println(s"Promedio de elementos: ${avg(sizes.map(_.toDouble))}")
        println(s"Mínimo: ${sizes.min}")
        println(s"Máximo: ${sizes.max}")
      }

  //  JSON → DISTRIBUCIÓN DE NOMBRES
  def analyzeJsonNames(
                        rows: Stream[IO, Map[String, String]],
                        column: String,
                        field: String,
                        top: Int = 10
                      ): IO[Unit] =
    rows
      .flatMap(r => Stream.emits(Cleaners.jsonNames(r(column), field)))
      .compile
      .toList
      .map { values =>
        println(s"\nColumna JSON ($column.$field)")
        values
          .groupBy(identity)
          .view
          .mapValues(_.size)
          .toList
          .sortBy(-_._2)
          .take(top)
          .foreach { case (v, c) =>
            println(f"$v%-25s -> $c")
          }
      }

  //  CREW (SIN PARSEAR JSON)
  def analyzeCrew(
                   rows: Stream[IO, Map[String, String]]
                 ): IO[Unit] =
    rows
      .map(_("crew"))
      .map(_.split(",").length.toDouble)
      .compile
      .toList
      .map { counts =>
        println("\nAnálisis Crew (texto plano)")
        println(s"Promedio de elementos: ${avg(counts)}")
        println(s"Mínimo: ${counts.min}")
        println(s"Máximo: ${counts.max}")
      }
}
