package utilities

import io.circe.*
import io.circe.parser.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

object Cleaners {

  //  STRING 
  def cleanString(s: String): Option[String] =
    Option(s)
      .map(_.trim)
      .filter(v => v.nonEmpty && v != "null" && v != "[]")

  //  DOUBLE 
  def toDouble(s: String): Option[Double] =
    Try(s.trim.toDouble).toOption.filter(!_.isNaN)

  //  INT 
  def toInt(s: String): Option[Int] =
    Try(s.trim.toInt).toOption

  //  FECHA 
  private val dateFormat = DateTimeFormatter.ofPattern("M/d/yyyy")

  def cleanDate(s: String): Option[LocalDate] =
    cleanString(s).flatMap { value =>
      Try(LocalDate.parse(value, dateFormat)).toOption
    }

  // Arreglar JSON con comillas simples
  def fixJsonQuotes(raw: String): String = {
    raw.replace("'", "\"")
  }

  //  JSON (REPARABLE) 
  def cleanJsonArray(raw: String): Option[Json] =
    cleanString(raw).flatMap { value =>
      val fixedValue = fixJsonQuotes(value)
      parse(fixedValue).toOption
    }

  //  JSON → CANTIDAD 
  def jsonArraySize(raw: String): Option[Int] =
    cleanJsonArray(raw).flatMap(_.asArray.map(_.size))

  //  JSON → LISTA DE NOMBRES 
  def jsonNames(raw: String, field: String): List[String] =
    cleanJsonArray(raw)
      .flatMap(_.asArray)
      .getOrElse(Vector())
      .flatMap(_.hcursor.get[String](field).toOption)
      .toList
}